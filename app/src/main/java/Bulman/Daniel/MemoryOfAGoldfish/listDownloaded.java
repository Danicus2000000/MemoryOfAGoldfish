package Bulman.Daniel.MemoryOfAGoldfish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class listDownloaded extends AppCompatActivity {
    private ArrayList<File> mFileList;//stores files
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_downloaded);
        mFileList=new ArrayList<>();//loads file directory and gets list of files were json is stored
        File directory=getApplicationContext().getFilesDir();
        File[] file=directory.listFiles();
        for(File toCheck : file)        //checks what json files we have
        {
            if(toCheck.getName().toUpperCase(Locale.ROOT).endsWith(".JSON"))
            {
                mFileList.add(toCheck);
            }
        }
        if(getSupportFragmentManager().getFragments().size()==0) { //ensures we don't recreate fragments twice
            for (int i = 0; i < mFileList.size(); i++) {
                FragmentTransaction initializer = getSupportFragmentManager().beginTransaction();
                CardSetFragment cardSet = CardSetFragment.newInstance(getCardSetName(mFileList.get(i)) + "  ", getHighScore(mFileList.get(i).getName()));
                initializer.add(R.id.CardHolder, cardSet, String.valueOf(i));
                initializer.commitNow();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        for(int i=0;i< getSupportFragmentManager().getFragments().size();i++)//loop through all fragments and add the data for viewing in
        {
            Fragment imageInitialised = getSupportFragmentManager().findFragmentByTag(String.valueOf(i));
            ImageView preview= imageInitialised.getView().findViewById(R.id.CardSetPreview);
            LinearLayout cardSet=imageInitialised.getView().findViewById(R.id.CardSetContainer);
            cardSet.setContentDescription(String.valueOf(i));
            preview.setImageBitmap(getPreviewImage(mFileList.get(i)));
        }
    }

    public void openDownloadPage(View view)//open downloader to get more card sets
    {
        Intent openDownloadPageIntent=new Intent(getApplicationContext(),getImageFromURLDisplay.class);
        startActivity(openDownloadPageIntent);
        finish();
    }

    public void openGameWindowDefault(View view) {//opens test game window
        Intent openGameWindowDefault=new Intent(getApplicationContext(),GameWindow.class);
        startActivity(openGameWindowDefault);
        finish();
    }
    private String getCardSetName(File file)//gets the card name from the JSON file
    {
        String cardSetName="";
        try
        {
            InputStream is = getApplicationContext().openFileInput(file.getName());
            int size=is.available();
            byte[] buffer=new byte[size];
            is.read(buffer);
            is.close();
            String jsonInput=new String(buffer,"UTF-8");
            JSONObject obj=new JSONObject(jsonInput);
            cardSetName=obj.getString("Name");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return cardSetName;
    }
    private Bitmap getPreviewImage(File pFile)//gets the preview image from the JSON file
    {
        Bitmap cardBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.goldfish1);
        String cardName="";
        try
        {
            InputStream is = getApplicationContext().openFileInput(pFile.getName());
            int size=is.available();
            byte[] buffer=new byte[size];
            is.read(buffer);
            is.close();
            String jsonInput=new String(buffer,"UTF-8");
            JSONObject obj=new JSONObject(jsonInput);
            cardName=obj.getString("TileBack").split("/")[1];
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        File directory= getApplicationContext().getDir("cardImages", Context.MODE_PRIVATE);
        File file=new File(directory,cardName);
        if(file.exists()){
            try{
                FileInputStream fileInputStream=new FileInputStream(file);
                cardBitmap= BitmapFactory.decodeStream(fileInputStream);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return cardBitmap;
    }
    private String getHighScore(String pPuzzleFileName)//gets the high score from the JSON file
    {
        String scoreToReturn="Best Score: ";
        SharedPreferences store=getApplicationContext().getSharedPreferences("scores",MODE_PRIVATE);
        if(store.contains(pPuzzleFileName))
        {
            scoreToReturn+=String.valueOf(store.getInt(pPuzzleFileName,MODE_PRIVATE));
        }
        else
        {
            scoreToReturn+="0";
        }
        return scoreToReturn;
    }
    public void selectCardSet(View view) {//when card set is selected load game window passing in the name of the json file picked
        LinearLayout cardSetSelected=(LinearLayout) view;
        File toParse=mFileList.get(Integer.parseInt(cardSetSelected.getContentDescription().toString()));
        Intent openGameWindow=new Intent(getApplicationContext(),GameWindow.class);
        openGameWindow.putExtra("cardSet",toParse.getName());
        startActivity(openGameWindow);
        finish();
    }

}