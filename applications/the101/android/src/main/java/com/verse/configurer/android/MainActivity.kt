package com.verse.configurer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.tagd.android.app.AppCompatActivity
import io.tagd.android.app.AwaitReadyLifeCycleEventsDispatcher
import io.tagd.android.app.TagdApplication


class MainActivity : AppCompatActivity() {

    private var flow: MainActivityFlow? = null

    override fun interceptOnCreate(savedInstanceState: Bundle?) {
        super.interceptOnCreate(savedInstanceState)
        flow = MainActivityFlow()
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    GreetingView("Greeting().greet()")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (awaitReadyLifeCycleEventsDispatcher().ready()) {
            onReady()
        } else {
            awaitReadyLifeCycleEventsDispatcher().register(this)
            onAwaiting()
        }
    }

    override fun needInjection(): Boolean {
        return false
    }

    override fun onInject() {
        // no-op
    }

    override fun awaitReadyLifeCycleEventsDispatcher(): AwaitReadyLifeCycleEventsDispatcher {
        return (application as TagdApplication).appService()!!
    }

    override fun onAwaiting() {
        //show the progress bar
    }

    override fun onReady() {
        flow?.trigger()
    }

    override fun onDestroy() {
        flow?.release()
        flow = null
        super.onDestroy()
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}

