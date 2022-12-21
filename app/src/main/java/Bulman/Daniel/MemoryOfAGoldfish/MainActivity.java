package Bulman.Daniel.MemoryOfAGoldfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
//todo Allow game to save state
//todo Fix fragment rebuild on GameWindow
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPlayButtonCLick(View view) { //open game window
        File directory=getApplicationContext().getFilesDir();
        File[] file=directory.listFiles();
        ArrayList<File> fileList=new ArrayList<>();//checks we have json files
        for(File toCheck : file)
        {
            if(toCheck.getName().toUpperCase(Locale.ROOT).endsWith(".JSON"))
            {
                fileList.add(toCheck);
            }
        }
        if(directory.exists() && fileList.size()>0)//if so open download list
        {
            Intent openSetHandlerIntent=new Intent(getApplicationContext(),listDownloaded.class);
            startActivity(openSetHandlerIntent);
        }
        else//otherwise jump straight to downloader
        {
            Toast.makeText(getApplicationContext(),"No downloaded card set.\nplease install one.",Toast.LENGTH_SHORT).show();
            Intent openURLHandlerIntent=new Intent(getApplicationContext(),getImageFromURLDisplay.class);
            startActivity(openURLHandlerIntent);
        }
    }

    public void onLoadButtonCLick(View view) { //open loaded game window
        ContextWrapper contextWrapper=new ContextWrapper(getApplicationContext());
        File directory=contextWrapper.getDir("saveData", Context.MODE_PRIVATE);
        if(directory.exists() && directory.listFiles().length>=1)//if a save file exists load game window
        {
            Intent openLoadHandlerIntent=new Intent(getApplicationContext(),GameWindow.class);
            startActivity(openLoadHandlerIntent);
        }
        else//if not ask user to play new game
        {
            Toast.makeText(getApplicationContext(),"No save file!\nPlease play a new game.",Toast.LENGTH_SHORT).show();
        }
    }
}