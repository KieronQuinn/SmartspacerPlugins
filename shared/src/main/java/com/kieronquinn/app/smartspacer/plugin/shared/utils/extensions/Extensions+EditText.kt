package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.TAP_DEBOUNCE
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce

fun EditText.onChanged() = callbackFlow {
    val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            //No-op
        }

        override fun afterTextChanged(p0: Editable?) {
            //No-op
        }

        override fun onTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {
            trySend(value)
        }
    }
    addTextChangedListener(textWatcher)
    awaitClose {
        removeTextChangedListener(textWatcher)
    }
}.debounce(TAP_DEBOUNCE)

fun EditText.onEditorActionSent(filter: Int? = null) = callbackFlow {
    val listener = TextView.OnEditorActionListener { _, actionId, _ ->
        trySend(actionId)
        filter?.let { actionId == filter } ?: true
    }
    setOnEditorActionListener(listener)
    awaitClose {
        setOnEditorActionListener(null)
    }
}.debounce(TAP_DEBOUNCE)