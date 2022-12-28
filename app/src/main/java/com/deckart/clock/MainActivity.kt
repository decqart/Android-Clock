package com.deckart.clock

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
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
                supportFragmentManager.commit {
                    if (tab?.position == 0) show(clock)
                    if (tab?.position == 1) show(stopWatch)
                    if (tab?.position == 2) show(timer)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                supportFragmentManager.commit {
                    if (tab?.position == 0) hide(clock)
                    if (tab?.position == 1) hide(stopWatch)
                    if (tab?.position == 2) hide(timer)
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
        val df = SimpleDateFormat("HH:mm:ss", Locale.US)

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
        val df = SimpleDateFormat("mm:ss.SSS", Locale.US)
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

        val timerText: TextView? = activity?.findViewById(R.id.timerText)

        val hourPicker: NumberPicker? = activity?.findViewById(R.id.hour_picker)
        hourPicker?.maxValue = 99
        val minutePicker: NumberPicker? = activity?.findViewById(R.id.minute_picker)
        minutePicker?.maxValue = 59
        val secondPicker: NumberPicker? = activity?.findViewById(R.id.second_picker)
        secondPicker?.maxValue = 59

        val startButton: Button? = activity?.findViewById(R.id.start)
        val cancelButton: Button? = activity?.findViewById(R.id.cancel)
        val pauseButton: Button? = activity?.findViewById(R.id.pause)

        var running: Boolean
        var pause = false

        startButton?.setOnClickListener {
            running = true
            startButton.visibility = View.INVISIBLE
            cancelButton?.visibility = View.VISIBLE
            pauseButton?.visibility = View.VISIBLE
            hourPicker?.visibility = View.INVISIBLE
            minutePicker?.visibility = View.INVISIBLE
            secondPicker?.visibility = View.INVISIBLE
            timerText?.visibility = View.VISIBLE
            var hour = hourPicker?.value
            var minute = minutePicker?.value
            var second = secondPicker?.value
            Thread {
                while (running) {
                    if (!pause) {
                        var hourString = hour.toString()
                        if (hourString.length < 2) hourString = "0$hourString"
                        var minuteString = minute.toString()
                        if (minuteString.length < 2) minuteString = "0$minuteString"
                        var secondString = second.toString()
                        if (secondString.length < 2) secondString = "0$secondString"
                        timerText?.post { timerText.text = "$hourString:$minuteString:$secondString" }
                        Thread.sleep(1000)
                        if (minute == 0 && hour != 0) {
                            hour = hour?.minus(1)
                            minute = 60
                        }
                        if (second == 0 && minute != 0) {
                            minute = minute?.minus(1)
                            second = 60
                        }
                        second = second?.minus(1)
                    }
                }
            }.start()
        }

        cancelButton?.setOnClickListener {
            startButton?.visibility = View.VISIBLE
            cancelButton.visibility = View.INVISIBLE
            pauseButton?.visibility = View.INVISIBLE
            hourPicker?.visibility = View.VISIBLE
            minutePicker?.visibility = View.VISIBLE
            secondPicker?.visibility = View.VISIBLE
            timerText?.visibility = View.INVISIBLE
            pauseButton?.text = "Pause"
            running = false
        }

        pauseButton?.setOnClickListener {
            if (pauseButton.text == "Pause") {
                pauseButton.text = "Resume"
                pause = true
            } else {
                pauseButton.text = "Pause"
                pause = false
            }
        }
    }
}
