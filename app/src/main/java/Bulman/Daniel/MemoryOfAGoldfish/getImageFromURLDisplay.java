package Bulman.Daniel.MemoryOfAGoldfish;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class getImageFromURLDisplay extends AppCompatActivity {
    private EditText mTextInput;//initialise global variables
    private Button mDownloadButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_image_from_urldisplay);//find views
        mTextInput=findViewById(R.id.editTextTextPersonName);
        mDownloadButton=findViewById(R.id.downloadButton);
        mTextInput.addTextChangedListener(new TextWatcher() {//add text input listener and use it to change button text
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {//if the text is no longer empty change user hint to show there is no longer a default value
                    if(mTextInput.getText().toString().equals(""))
                    {
                        mDownloadButton.setText(getResources().getString(R.string.downloadDefaultData));
                    }
                    else
                    {
                        mDownloadButton.setText(getResources().getString(R.string.downloadJSONButton));
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {//if enter on keyboard is pressed act as if download is pressed
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if((keyEvent!=null && (keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER))|| i== EditorInfo.IME_ACTION_DONE)
                {
                    onDownloadButtonCLick(mDownloadButton);
                }
                return false;
            }
        });
    }

    public void onDownloadButtonCLick(View view) {//checks JSON for download is valid
        if(mTextInput.getText().toString().equals(""))//if there is no text in the input field use default url
        {
            String defaultAddress="https://goparker.com/600096/memory/goldfish/index.json";
            checkForCopy(defaultAddress);
        }
        else
        {
            if (mTextInput.getText().toString().toUpperCase(Locale.ROOT).endsWith(".JSON"))//if the file ends with .JSON suffix
            {
                String targetAddress = mTextInput.getText().toString();
                checkForCopy(targetAddress);
            }
            else//otherwise display error
            {
                Toast.makeText(getApplicationContext(), "Invalid JSON URL!", Toast.LENGTH_SHORT).show();

            }
        }
    }
    private void checkForCopy(String pDownloadUrl)//checks the url to see if the file has already been downloaded
    {
        File directory=getApplicationContext().getFilesDir();
        File[] file=directory.listFiles();
        String[] addressSegments=pDownloadUrl.split("/");
        String fileName;
        try//checks filename is parsable if not auto fails validation
        {
            fileName = addressSegments[addressSegments.length - 2] + addressSegments[addressSegments.length - 1];
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),"Invalid JSON URL!",Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isDownloaded=false;
        for(File toCheck : file)
        {
            if(toCheck.getName().equals(fileName))
            {
                isDownloaded=true;
                break;
            }
        }
        if(isDownloaded)//if it has already been downloaded display overwrite warning
        {
            AlertDialog.Builder warning=new AlertDialog.Builder(this);
            warning.setMessage("A Card set with the same name already exists would you like to overwrite it?");
            warning.setTitle("Card Set Duplicate");
            warning.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDownload(pDownloadUrl,fileName);
                }
            });
            warning.setNegativeButton("Cancel", null);
            warning.setCancelable(true);
            warning.create().show();
        }
        else
        {
            doDownload(pDownloadUrl,fileName);
        }
    }
    private void doDownload(String pDownloadUrl,String fileName)//does the file downloading
    {
        Toast.makeText(getApplicationContext(),"Attempting to Download file",Toast.LENGTH_SHORT).show();//shows user downloading has begun
        RemoteTileGenerator remoteInstall=RemoteTileGenerator.getInstance(getApplicationContext(),pDownloadUrl);//gets downloader
        LiveData<ArrayList<Tile>> remoteData=remoteInstall.loadTilesFromJSON();//downloads and saves
        remoteData.observe(this, new Observer<ArrayList<Tile>>() {
            @Override
            public void onChanged(ArrayList<Tile> tiles) {//once tileset is saved download must be completed
                if(tiles.size()==20){
                    for(int i=0;i<tiles.size();i++)
                    {
                        remoteInstall.getTile(i);
                    }
                    Toast.makeText(getApplicationContext(),"Downloaded "+fileName+" successfully",Toast.LENGTH_SHORT).show();//notify user and close window
                    finish();
                }
            }
        });
    }
}