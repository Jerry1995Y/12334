package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPrefs
import com.example.model.TaskItem
import com.example.notification.Notifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("一人居", appName)
    }

    @Test
    fun `test app prefs tasks and backup serialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPrefs(context)

        val initialTasks = prefs.getTasks()
        assertTrue(initialTasks.isNotEmpty())

        val testTasks = listOf(
            TaskItem(100L, "测试待办1", done = false),
            TaskItem(101L, "测试待办2", done = true)
        )
        prefs.saveTasks(testTasks)

        val retrieved = prefs.getTasks()
        assertEquals(2, retrieved.size)
        assertEquals("测试待办1", retrieved[0].text)
        assertEquals(true, retrieved[1].done)

        // Test JSON Export & Import
        val json = prefs.exportBackupJson()
        assertTrue(json.contains("测试待办1"))

        val importSuccess = prefs.importBackupJson(json)
        assertTrue(importSuccess)
    }

    @Test
    fun `test notifier channels init`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Notifier.initChannels(context)
        assertNotNull(Notifier.getFluidCloudStatusText(context))
    }
}
