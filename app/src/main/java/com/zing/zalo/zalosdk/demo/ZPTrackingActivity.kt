package com.zing.zalo.zalosdk.demo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zing.zalo.tracking.pixel.Tracker
import com.zing.zalo.tracking.pixel.impl.Storage
import java.util.*

class ZPTrackingActivity : AppCompatActivity() {

    private lateinit var addParamsButton: Button
    private lateinit var submitButton: Button
    private lateinit var mainLinearLayout: LinearLayout
    private lateinit var paramsLinearLayout: LinearLayout
    private lateinit var resultTextView: TextView
    private lateinit var eventInfoTextView: TextView
    private lateinit var eventNameEditTextView: EditText
    private lateinit var context: Context
    private lateinit var tracker: Tracker
    private var handler = Handler()


    private val delay = 1500L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zptracking)

        configureUi()
        configureLogic()

        generateParams()

        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    val f = Tracker::class.java.getDeclaredField("mStorage")
                    f.isAccessible = true

                    val storage =
                        f.get(Tracker.newInstance(context, 6486531153301779475L)) as Storage
                    val eventInfo = "${storage.events.size} events in Storage"
                    eventInfoTextView.text = eventInfo
                    handler.postDelayed(this, delay)
                } catch (ex: NoSuchFieldException) {
                } catch (ex: IllegalAccessException) {
                }
            }
        }, delay)



        bindViewListener()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    //#region private method

    private fun configureUi() {
        submitButton = findViewById(R.id.submit_button)
        addParamsButton = findViewById(R.id.add_params_button)
        mainLinearLayout = findViewById(R.id.zp_tracking_linear_layout)
        paramsLinearLayout = findViewById(R.id.params_linear_layout)
        resultTextView = findViewById(R.id.result_text_view)
        eventInfoTextView = findViewById(R.id.event_info_text_view)
        eventNameEditTextView = findViewById(R.id.event_name_edit_text_view)

    }

    private fun configureLogic() {
        context = this
        tracker = Tracker.newInstance(this, 6486531153301779475L)
        tracker.setAppId(getString(R.string.zalosdk_app_id))
    }

    private fun generateParams() {
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        val keyTextView = generateKeyEditTextView()
        val valueTextView = generateValueEditTextView()
        val removeButton = generateRemoveButton()
        linearLayout.addView(keyTextView)
        linearLayout.addView(valueTextView)
        linearLayout.addView(removeButton)
        paramsLinearLayout.addView(linearLayout)
    }


    private fun bindViewListener() {
        submitButton.setOnClickListener {
            val params: MutableMap<String, Any> =
                HashMap()
            val paramsCount = paramsLinearLayout.childCount
            if (paramsCount == 0) return@setOnClickListener
            for (i in 0 until paramsCount) {
                val paramLayout =
                    paramsLinearLayout.getChildAt(i) as LinearLayout
                val keyEdiText =
                    paramLayout.getChildAt(0) as EditText
                val valueEditText =
                    paramLayout.getChildAt(1) as EditText
                val key = keyEdiText.text.toString()
                val value = valueEditText.text.toString()
                params[key] = value
            }
            val eventName = eventNameEditTextView.text.toString()
            if (eventName.isEmpty()) return@setOnClickListener
            tracker.track(eventName, params)
            resultTextView.text = params.toString()
        }

        addParamsButton.setOnClickListener {
            generateParams()
        }
    }


    private fun generateRemoveButton(): Button {
        val button = Button(this)
        val param =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.3f
            )
        button.text = "-"
        button.layoutParams = param
        button.setOnClickListener { v ->
            val linearParent =
                v.parent.parent as LinearLayout
            val linearChild =
                v.parent as LinearLayout
            linearParent.removeView(linearChild)
        }
        return button
    }

    private fun generateKeyEditTextView(): EditText {
        val editTextView = EditText(this)
        val param =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1F
            )
        editTextView.hint = "Key"
        editTextView.layoutParams = param
        return editTextView
    }

    private fun generateValueEditTextView(): EditText {
        val editTextView = EditText(this)
        val param =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1F
            )
        editTextView.hint = "Value"
        editTextView.layoutParams = param
        return editTextView
    }
    //#endregion
}
