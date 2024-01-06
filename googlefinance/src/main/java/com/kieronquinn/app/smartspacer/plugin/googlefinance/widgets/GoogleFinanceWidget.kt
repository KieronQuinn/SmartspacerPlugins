package com.kieronquinn.app.smartspacer.plugin.googlefinance.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.googlefinance.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlefinance.GoogleFinancePlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.FinancialWidget.FinancialItem
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.GoogleFinanceRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.RemoteAdapter
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.ViewGroup
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.mapWidgetViewStructure
import org.koin.android.ext.android.inject


class GoogleFinanceWidget: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widgets.googlefinance"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "com.google.android.apps.search.widgets.stocks.StocksWidgetReceiver"
        )

        private const val IDENTIFIER_LIST = "list"

        private const val IDENTIFIER_PRICE_TREND = "price_trend"
        private const val IDENTIFIER_CLOSING_PRICE = "closing_price"
        private const val IDENTIFIER_NAME = "name"
        private const val IDENTIFIER_TREND = "trend"
        private const val IDENTIFIER_PRICE_PREFIX = "price_prefix"
        private const val IDENTIFIER_PRICE_FIRST = "price_first"
        private const val IDENTIFIER_PRICE_SECOND = "price_second"
        private const val IDENTIFIER_DIRECTION = "direction"

        private val STRUCTURE_ROOT: ViewGroup.() -> Unit = {
            relativeLayout {
                relativeLayout {
                    linearLayout {
                        index = 1
                        linearLayout {
                            listView {
                                id = IDENTIFIER_LIST
                            }
                        }
                    }
                }
            }
        }

        private val STRUCTURE_ITEM: ViewGroup.() -> Unit = {
            relativeLayout {
                relativeLayout {
                    relativeLayout {
                        relativeLayout {
                            linearLayout {
                                linearLayout {
                                    linearLayout(STRUCTURE_ITEM_TOP)
                                    relativeLayout(STRUCTURE_ITEM_RIGHT)
                                }
                                relativeLayout(STRUCTURE_ITEM_BOTTOM)
                            }
                        }
                    }
                }
            }
        }

        private val STRUCTURE_ITEM_TOP: ViewGroup.() -> Unit = {
            linearLayout {
                textView {
                    id = IDENTIFIER_PRICE_PREFIX
                }
                textView {
                    id = IDENTIFIER_PRICE_FIRST
                }
                textView {
                    id = IDENTIFIER_PRICE_SECOND
                }
            }
            linearLayout {
                textView {
                    id = IDENTIFIER_NAME
                }
                textView {
                    id = IDENTIFIER_TREND
                }
            }
        }

        private val STRUCTURE_ITEM_RIGHT: ViewGroup.() -> Unit = {
            imageView {
                index = 1
                id = IDENTIFIER_DIRECTION
            }
        }

        private val STRUCTURE_ITEM_BOTTOM: ViewGroup.() -> Unit = {
            imageView {
                id = IDENTIFIER_PRICE_TREND
            }
            imageView {
                id = IDENTIFIER_CLOSING_PRICE
            }
        }

        private val STRUCTURE_ITEM_NO_CHART: ViewGroup.() -> Unit = {
            relativeLayout {
                relativeLayout {
                    relativeLayout {
                        relativeLayout {
                            linearLayout {
                                index = 1
                                linearLayout {
                                    textView {
                                        id = IDENTIFIER_PRICE_FIRST
                                    }
                                    textView {
                                        id = IDENTIFIER_PRICE_SECOND
                                    }
                                }
                                linearLayout {
                                    linearLayout {
                                        textView {
                                            id = IDENTIFIER_NAME
                                        }
                                        textView {
                                            id = IDENTIFIER_TREND
                                        }
                                    }
                                    relativeLayout {
                                        imageView {
                                            index = 1
                                            id = IDENTIFIER_DIRECTION
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }
    }

    private val googleFinanceRepository by inject<GoogleFinanceRepository>()

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val structure = mapWidgetViewStructure(views, STRUCTURE_ROOT) ?: return
        val listViewId = structure.getViewIdFromStructureId(IDENTIFIER_LIST) ?: return
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getRemoteCollectionItems(smartspacerId, listViewId)
        }else{
            getAdapter(smartspacerId, listViewId)
        }
    }

    override fun onAdapterConnected(smartspacerId: String, adapter: RemoteAdapter) {
        super.onAdapterConnected(smartspacerId, adapter)
        val count = adapter.getCount() - 1
        val views = adapter.getViewAt(0)?.remoteViews?.load() ?: return
        val structure = mapWidgetViewStructure(views, STRUCTURE_ITEM) ?: return
        val name = structure.findViewByStructureId<TextView>(views, IDENTIFIER_NAME)
        val trend = structure.findViewByStructureId<TextView>(views, IDENTIFIER_TREND)
        val pricePrefix = structure.findViewByStructureId<TextView>(views, IDENTIFIER_PRICE_PREFIX)
        val priceFirst = structure.findViewByStructureId<TextView>(views, IDENTIFIER_PRICE_FIRST)
        val priceSecond = structure.findViewByStructureId<TextView>(views, IDENTIFIER_PRICE_SECOND)
        val direction = structure.findViewByStructureId<ImageView>(views, IDENTIFIER_DIRECTION)
        val priceTrend = structure.findViewByStructureId<ImageView>(views, IDENTIFIER_PRICE_TREND)
        val closingPrice = structure.findViewByStructureId<ImageView>(views, IDENTIFIER_CLOSING_PRICE)
        val priceTrendImage = priceTrend?.drawable?.toBitmap() ?: return
        val closingPriceImage = closingPrice?.drawable?.toBitmap() ?: return
        val items = if(count > 1){
            ArrayList<FinancialItem>().apply {
                for (i in 1 until count) {
                    adapter.getViewAt(i)?.remoteViews?.loadFinancialItem()?.let {
                        add(it)
                    }
                }
            }
        }else emptyList()
        googleFinanceRepository.setFinancialWidget(
            smartspacerId,
            name?.text?.toString() ?: return,
            trend?.text?.toString() ?: return,
            pricePrefix?.text?.toString() ?: return,
            priceFirst?.text?.toString() ?: return,
            priceSecond?.text?.toString() ?: return,
            direction?.drawable?.toBitmap() ?: return,
            priceTrendImage,
            closingPriceImage,
            items
        )
    }

    private fun RemoteViews.loadFinancialItem(): FinancialItem? {
        val views = load() ?: return null
        val structure = mapWidgetViewStructure(views, STRUCTURE_ITEM_NO_CHART) ?: return null
        val name = structure.findViewByStructureId<TextView>(views, IDENTIFIER_NAME)
        val trend = structure.findViewByStructureId<TextView>(views, IDENTIFIER_TREND)
        val priceFirst = structure.findViewByStructureId<TextView>(views, IDENTIFIER_PRICE_FIRST)
        val priceSecond = structure.findViewByStructureId<TextView>(views, IDENTIFIER_PRICE_SECOND)
        val direction = structure.findViewByStructureId<ImageView>(views, IDENTIFIER_DIRECTION)
        return FinancialItem(
            name?.text?.toString() ?: return null,
            trend?.text?.toString() ?: return null,
            priceFirst?.text?.toString() ?: return null,
            priceSecond?.text?.toString() ?: return null,
            direction?.drawable?.toBitmap() ?: return null
        )
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            width = 150,
            height = 400
        )
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

}