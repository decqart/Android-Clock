package com.deckart.clock

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clock = Clock()
        val stopWatch = StopWatch()
        val timer = Timer()

        supportFragmentManager.commit {
            add(R.id.fragment_container, clock)
            add(R.id.fragment_container, stopWatch)
            add(R.id.fragment_container, timer)
            hide(stopWatch)
            hide(timer)
        }

        val tabs: TabLayout = findViewById(R.id.tabs)

        tabs.addTab(tabs.newTab().setText(R.string.tab_text_1))
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_2))
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_3))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    supportFragmentManager.commit {
                        show(clock)
                    }
                }
                if (tab?.position == 1) {
                    supportFragmentManager.commit {
                        show(stopWatch)
                    }
                }
                if (tab?.position == 2) {
                    supportFragmentManager.commit {
                        show(timer)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    supportFragmentManager.commit {
                        hide(clock)
                    }
                }
                if (tab?.position == 1) {
                    supportFragmentManager.commit {
                        hide(stopWatch)
                    }
                }
                if (tab?.position == 2) {
                    supportFragmentManager.commit {
                        hide(timer)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}

class Clock : Fragment(R.layout.clock) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val txt: TextView? = activity?.findViewById(R.id.textView)
        val df: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        Thread {
            while (true) {
                val date = df.format(Calendar.getInstance().time)
                txt?.post { txt.text = date }
                Thread.sleep(1000)
            }
        }.start()
    }
}

class StopWatch : Fragment(R.layout.stop_watch) {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txt: TextView? = activity?.findViewById(R.id.texty)
        val button: Button? = activity?.findViewById(R.id.sw_button)
        val button2: Button? = activity?.findViewById(R.id.lap_button)
        val df: DateFormat = SimpleDateFormat("mm:ss.SSS", Locale.US)
        df.timeZone = TimeZone.getTimeZone("GMT")

        var millis = 0
        var running = false

        button?.setOnClickListener {
            val thread = Thread {
                while (running) {
                    val date = df.format(millis)
                    txt?.post { txt.text = date.substring(0, date.length-1) }
                    Thread.sleep(1)
                    millis++
                }
            }

            if (button.text == "Start" || button.text == "Resume") {
                button.text = "Stop"
                button2?.text = "Lap"
                button2?.isEnabled = true
                running = true
                thread.start()
            } else if (button.text == "Stop") {
                button.text = "Resume"
                button2?.text = "Reset"
                running = false
                thread.join()
            }
        }

        button2?.setOnClickListener {
            if (button2.text == "Reset") {
                button?.text = "Start"
                button2.text = "Lap"
                button2.isEnabled = false
                millis = 0
                txt?.text = "00:00.00"
            }
        }
    }
}

class Timer : Fragment(R.layout.timer) {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hourPicker: NumberPicker? = activity?.findViewById(R.id.hour_picker)
        hourPicker?.maxValue = 99
        val minutePicker: NumberPicker? = activity?.findViewById(R.id.minute_picker)
        minutePicker?.maxValue = 59
        val secondPicker: NumberPicker? = activity?.findViewById(R.id.second_picker)
        secondPicker?.maxValue = 59

        val startButton: Button? = activity?.findViewById(R.id.start)
        val cancelButton: Button? = activity?.findViewById(R.id.cancel)
        val pauseButton: Button? = activity?.findViewById(R.id.pause)

        startButton?.setOnClickListener {
            startButton.visibility = View.INVISIBLE
            cancelButton?.visibility = View.VISIBLE
            pauseButton?.visibility = View.VISIBLE
            val timeInMillis = secondPicker?.value?.let { num ->
                minutePicker?.value?.times(60000)?.plus(num.times(1000))?.let {
                    hourPicker?.value?.times(3_600_000)
                        ?.plus(it)
                }
            }?.toLong()

            object : CountDownTimer(timeInMillis!!, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.e("seconds remaining: " + millisUntilFinished / 1000, "TAG")
                }

                override fun onFinish() {
                    Log.e("YAY", "finished")
                }
            }.start()
        }

        cancelButton?.setOnClickListener {
            startButton?.visibility = View.VISIBLE
            cancelButton.visibility = View.INVISIBLE
            pauseButton?.visibility = View.INVISIBLE
            pauseButton?.text = "Pause"
        }

        pauseButton?.setOnClickListener {
            if (pauseButton.text == "Pause") {
                pauseButton.text = "Resume"
            } else {
                pauseButton.text = "Pause"
            }
        }
    }
}
