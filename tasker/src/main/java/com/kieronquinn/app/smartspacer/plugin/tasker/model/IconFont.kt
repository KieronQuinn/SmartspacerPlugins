package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.getIconOrNull
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.ITypeface
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesomeBrand
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesomeRegular
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.RoundedGoogleMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.SharpGoogleMaterial
import com.mikepenz.iconics.typeface.library.phosphor.Phosphor

enum class IconFont(val icon: IIcon, vararg val typeface: ITypeface) {

    MATERIAL_REGULAR(GoogleMaterial.Icon.gmd_perm_media, GoogleMaterial),
    MATERIAL_OUTLINED(OutlinedGoogleMaterial.Icon.gmo_perm_media, OutlinedGoogleMaterial),
    MATERIAL_SHARP(SharpGoogleMaterial.Icon.gms_perm_media, SharpGoogleMaterial),
    MATERIAL_ROUNDED(RoundedGoogleMaterial.Icon.gmr_perm_media, RoundedGoogleMaterial),
    FONT_AWESOME(FontAwesomeBrand.Icon.fab_font_awesome, FontAwesome, FontAwesomeRegular, FontAwesomeBrand),
    COMMUNITY_MATERIAL(CommunityMaterial.Icon3.cmd_material_design, CommunityMaterial),
    PHOSPHOR(Phosphor.Icon2.pho_phosphor_logo, Phosphor);

    fun getIcon(key: String): IIcon? {
        return typeface.firstNotNullOfOrNull { it.getIconOrNull(key) }
    }

}