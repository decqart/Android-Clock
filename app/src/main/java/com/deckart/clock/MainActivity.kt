package com.deckart.clock

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
                        hide(stopWatch)
                        hide(timer)
                        show(clock)
                    }
                }
                if (tab?.position == 1) {
                    supportFragmentManager.commit {
                        hide(clock)
                        hide(timer)
                        show(stopWatch)
                    }
                }
                if (tab?.position == 2) {
                    supportFragmentManager.commit {
                        hide(clock)
                        hide(stopWatch)
                        show(timer)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}

class Clock : Fragment(R.layout.clock) {
    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val txt: TextView? = activity?.findViewById(R.id.textView)
        val df: DateFormat = SimpleDateFormat("HH:mm:ss")

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
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txt: TextView? = activity?.findViewById(R.id.texty)
        val button: Button? = activity?.findViewById(R.id.button)
        val df: DateFormat = SimpleDateFormat("HH:mm:ss")
        df.timeZone = TimeZone.getTimeZone("GMT")

        var seconds = 0
        var running = false

        Thread {
            while (true) {
                if (running) {
                    val date = df.format(seconds * 1000)
                    txt?.post { txt.text = date }
                    Thread.sleep(1000)
                    seconds++
                }
            }
        }.start()

        button?.setOnClickListener {
            if (button.text == "Start") {
                button.text = "Stop"
                running = true
            } else {
                button.text = "Start"
                running = false
                seconds = 0
                txt?.text = "00:00:00"
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
            startButton.visibility = INVISIBLE
            cancelButton?.visibility = VISIBLE
            pauseButton?.visibility = VISIBLE
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
            startButton?.visibility = VISIBLE
            cancelButton.visibility = INVISIBLE
            pauseButton?.visibility = INVISIBLE
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
