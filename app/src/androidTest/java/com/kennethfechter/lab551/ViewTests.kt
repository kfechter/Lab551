package com.kennethfechter.lab551

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kennethfechter.lab551.appcore.readAnalytics
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ViewTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val activity = activityScenarioRule<NFCActivity>()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun verifyThemeDialogButton() {
        Espresso.onView(withId(R.id.day_night_mode))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun verifyAnalyticsButton() {
        Espresso.onView(withId(R.id.analytics_opt_status))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun verifyAboutApplicationButton() {
        Espresso.onView(withId(R.id.about_application))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun verifyAboutDialog() {
        Espresso.onView(withId(R.id.about_application)).perform(ViewActions.click())
        val dialogTitle = "Calculendar Developers"
        Espresso.onView(ViewMatchers.withText(dialogTitle))
            .inRoot(RootMatchers.isDialog()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withText("Kenneth Fechter"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withText("OK"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(dialogTitle)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun verifyDialogIntent() {
        Espresso.onView(withId(R.id.about_application)).perform(ViewActions.click())

        val kfechterLinkedInUrl = "https://www.linkedin.com/in/kafechter/"

        Intents.init()

        val expectedIntentkfechter =
            Matchers.allOf(hasAction(Intent.ACTION_VIEW), hasData(kfechterLinkedInUrl))
        intending(expectedIntentkfechter).respondWith(Instrumentation.ActivityResult(0, null))
        Espresso.onView(ViewMatchers.withText("Kenneth Fechter")).perform(ViewActions.click())
        intended(expectedIntentkfechter)

        Intents.release()
    }

    @Test
    fun verifyThemeDialog() {
        val testAutoThemeMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val dialogText = context.getString(R.string.theme_dialog_title)

        Espresso.onView(withId(R.id.day_night_mode)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(dialogText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.radio_day_mode))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.radio_night_mode))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.radio_battery_mode))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.radio_day_mode)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
        var currentDayNightMode = AppCompatDelegate.getDefaultNightMode()
        Assert.assertEquals("The Expected Mode does not match", AppCompatDelegate.MODE_NIGHT_NO, currentDayNightMode)

        Espresso.onView(withId(R.id.day_night_mode)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.radio_night_mode)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        currentDayNightMode = AppCompatDelegate.getDefaultNightMode()
        Assert.assertEquals("The Expected Mode does not match", AppCompatDelegate.MODE_NIGHT_YES, currentDayNightMode)

        Espresso.onView(withId(R.id.day_night_mode)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.radio_battery_mode)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        currentDayNightMode = AppCompatDelegate.getDefaultNightMode()
        Assert.assertEquals("The Expected Mode does not match", AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, currentDayNightMode)

        if (testAutoThemeMode) {
            Espresso.onView(withId(R.id.day_night_mode)).perform(ViewActions.click())
            Espresso.onView(withId(R.id.radio_auto_mode))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(withId(R.id.radio_auto_mode)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

            currentDayNightMode = AppCompatDelegate.getDefaultNightMode()
            Assert.assertEquals("The Expected Mode does not match", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, currentDayNightMode)
        }

    }

    @Test
    fun verifyAnalyticsDialog() {
        val dialogText = context.getString(R.string.opt_in_dialog_message)

        val optInText = "Opt-In"
        val optOutText = "Opt-Out"

        Espresso.onView(withId(R.id.analytics_opt_status))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(dialogText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withText(optInText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withText(optOutText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun validateOptOut() {
        Espresso.onView(withId(R.id.analytics_opt_status))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText("Opt-Out"))
            .perform(ViewActions.click())

        Assert.assertEquals("The current analytics value does not match the expected value", 0, getValue(context.readAnalytics().asLiveData()))
    }

    @Test
    fun validateOptIn() {
        Espresso.onView(withId(R.id.analytics_opt_status))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText("Opt-In"))
            .perform(ViewActions.click())

        Assert.assertEquals("The current analytics value does not match the expected value", 1, getValue(context.readAnalytics().asLiveData()))
    }

    // Copied from stackoverflow
    @Suppress("UNCHECKED_CAST")
    @Throws(InterruptedException::class)
    fun <Int> getValue(liveData: LiveData<Int>): Int {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<Int> {
            override fun onChanged(t: Int) {
                data[0] = t
                latch.countDown()
                liveData.removeObserver(this) //To change body of created functions use File | Settings | File Templates.
            }

        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        return data[0] as Int
    }

}