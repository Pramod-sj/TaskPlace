package com.example.pramod.taskplace;

import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;
import com.stephentuso.welcome.WelcomeHelper;

/**
 * Created by pramod on 27/1/18.
 */

public class StartPage extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .page(new BasicPage(R.drawable.ic_insert_emoticon_black_24dp,"Welcome to TaskPlace","Never miss uh task again!").background(R.color.orange))
                .page(new BasicPage(R.drawable.ic_task_white_24dp,"Any Task....?","It's easy to set or view task").background(R.color.purple))
                .page(new BasicPage(R.drawable.ic_notifications_white_24dp,"Get notified","get instant notification about uh task whenever uh reach near place").background(R.color.colorPrimaryDark))
                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.fade_out)
                .build();
    }

}
