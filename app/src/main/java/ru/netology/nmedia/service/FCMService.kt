package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    private val action = "action"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val checkRecipientId = AppAuth.getInstance().authStateFlow.value.id
        val putPushMessage = gson.fromJson(message.data[content], PushMessage::class.java)

        //recipientId = тому, что в AppAuth, то всё ok, показываете Notification;
        if (putPushMessage.recipientId == checkRecipientId) {
            handleUser(putPushMessage)
        } else if (putPushMessage.recipientId == null)
        //если recipientId = null, то это массовая рассылка, показываете Notification
        {
            handleMassMailing(putPushMessage)
        } else if (putPushMessage.recipientId == 0L && putPushMessage.recipientId != checkRecipientId)
        //если recipientId = 0 (и не равен вашему), сервер считает, что у вас анонимная
        // аутентификация и вам нужно переотправить свой push token
        {
            errorAuthorization(putPushMessage)
            AppAuth.getInstance().sendPushToken()
        } else if (putPushMessage.recipientId != 0L && putPushMessage.recipientId != checkRecipientId)
        //если recipientId != 0 (и не равен вашему), значит сервер считает, что на вашем
        // устройстве другая аутентификация и вам нужно переотправить свой push token;
            incorrectAuthorization(putPushMessage)
        AppAuth.getInstance().sendPushToken()
    }


    /** -----------------------------------------------------------------------------------------**/
    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }


    /** --------если recipientId = null, то это массовая рассылка, показываете Notification------**/
    private fun handleMassMailing(content: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_mass_mailing,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)

    }

    /** ----пользовательвошел в систему----------------------------------------------------------**/
    private fun handleUser(content: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_wellcome,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    /** ----не авторизован-----------------------------------------------------------------------**/
    private fun errorAuthorization(content: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setShowWhen(false)
            .setContentTitle(
                getString(
                    R.string.authorization_error
                )
            )
            .setContentText(
                getString(
                    R.string.authorization_error_notification
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    /** ----другая авторизация-------------------------------------------------------------------**/
    private fun incorrectAuthorization(content: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setShowWhen(false)
            .setContentTitle(
                getString(
                    R.string.incorrect_authorization
                )
            )
            .setContentText(
                getString(
                    R.string.authorization_error_notification
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}

/** ---------------------------------------------------------------------------------------------**/

/** класся **/

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)

