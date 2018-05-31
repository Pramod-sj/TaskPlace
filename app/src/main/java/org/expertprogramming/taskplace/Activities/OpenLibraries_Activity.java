package org.expertprogramming.taskplace.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.expertprogramming.taskplace.Adapters.OpenLibrariesAdapter;
import org.expertprogramming.taskplace.R;

/**
 * Created by pramod on 26/1/18.
 */

public class OpenLibraries_Activity extends AppCompatActivity {
    ListView listView;
    String[] LibDesc={
            getString(R.string.circleimagedesc),
            getString(R.string.picassodesc),
            getString(R.string.customcrashdesc),
            getString(R.string.welcomescreendesc),
            getString(R.string.toastydesc)
    };
    String[] LibName={
                    "CircleImageView",
                    "Picasso",
                    "CustomActivityOnCrash",
                    "Welcome",
                    "Toasty"
                        };
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_openlibraries);
        listView=findViewById(R.id.openLibrariesList);
        setTitle("Open Source Libraries");
        OpenLibrariesAdapter adapter=new OpenLibrariesAdapter(OpenLibraries_Activity.this,LibName,LibDesc);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri;
                switch(position){
                    case 0:
                        uri=Uri.parse("https://github.com/Clans/FloatingActionButton");
                        startActivity(new Intent(Intent.ACTION_VIEW,uri));
                        break;
                    case 1:
                        uri=Uri.parse("https://github.com/hdodenhof/CircleImageView");
                        startActivity(new Intent(Intent.ACTION_VIEW,uri));
                        break;
                    case 2:
                        uri=Uri.parse("https://github.com/square/picasso");
                        startActivity(new Intent(Intent.ACTION_VIEW,uri));
                    case 3:
                        uri=Uri.parse("https://github.com/stephentuso/welcome-android");
                        startActivity(new Intent(Intent.ACTION_VIEW,uri));
                    case 4:
                        uri=Uri.parse("https://github.com/GrenderG/Toasty");
                        startActivity(new Intent(Intent.ACTION_VIEW,uri));
                }
            }
        });
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onStop(){
        super.onStop();
    }

    protected void onDestroy(){
        finish();
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
