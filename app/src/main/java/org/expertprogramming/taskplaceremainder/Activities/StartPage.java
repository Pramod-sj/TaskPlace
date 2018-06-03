package org.expertprogramming.taskplaceremainder.Activities;

import org.expertprogramming.taskplaceremainder.R;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

/**
 * Created by pramod on 27/1/18.
 */

public class StartPage extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .page(new BasicPage(R.drawable.ic_insert_emoticon_black_24dp,"Welcome to TaskPlace","Never miss uh task again!").background(R.color.colorPrimary))
                .page(new BasicPage(R.drawable.ic_task_white_24dp,"Any Task....?","It's easy to set or view tasks..").background(R.color.purple))
                .page(new BasicPage(R.drawable.ic_notifications_white_24dp,"Get notified","Get instant notification about your task..").background(R.color.colorPrimaryDark))
                .page(new BasicPage(R.drawable.ic_settings_white_24dp,"Settings","Change ringtone, notification type, vibration and many more..").background(R.color.blue))
                .page(new BasicPage(R.drawable.ic_directions_black_24dp,"Get Direction","Get direction for any of your task..").background(R.color.colorAccent))
                .swipeToDismiss(false)
                .exitAnimation(android.R.anim.fade_out)
                .build();
    }

}
