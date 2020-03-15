package com.mati1.codelabs.notificationscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val serviceName: ComponentName
        get() = ComponentName(packageName, NotificationJobService::class.java.name)

    private val scheduler: JobScheduler
        get() = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler

    private val seekBarSet: Boolean
        get() = seekBar.progress > 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) =
                    if (progress > 0) {
                        seekBarProgress.setText("$progress s");
                    } else {
                        seekBarProgress.text = "Not Set";
                    }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private val jobNetworkType: Int
        get() = when (networkOptions.checkedRadioButtonId) {
            R.id.anyNetwork -> JobInfo.NETWORK_TYPE_ANY
            R.id.wifiNetwork -> JobInfo.NETWORK_TYPE_UNMETERED
            R.id.noNetwork -> JobInfo.NETWORK_TYPE_NONE
            else -> JobInfo.NETWORK_TYPE_NONE
        }

    fun scheduleJob(view: View) {
        val builder = JobInfo.Builder(JOB_ID, serviceName)

        builder.apply {
            setRequiresDeviceIdle(idleSwitch.isChecked)
            setRequiresCharging(chargingSwitch.isChecked)
            setRequiredNetworkType(jobNetworkType)
            if (seekBarSet) setOverrideDeadline(seekBar.progress * 1000L)
        }

        val constraintSet = (jobNetworkType != JobInfo.NETWORK_TYPE_NONE
                || idleSwitch.isChecked || chargingSwitch.isChecked) || seekBarSet

        if (constraintSet) {
            scheduler.schedule(builder.build())

            Toast.makeText(this, "Job Scheduled, job will run when " +
                    "the constraints are met.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please set at least one constraint",
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs(view: View) = scheduler.cancelAll().also {
        Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
    }

    companion object {
        private const val JOB_ID = 0
    }
}
