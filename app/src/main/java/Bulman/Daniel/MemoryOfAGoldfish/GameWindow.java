package Bulman.Daniel.MemoryOfAGoldfish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class GameWindow extends AppCompatActivity{
    private ArrayList<Tile> mCards;//stores all cards and their associated data
    private int mTurned;//stores number of tiles turned over
    private int mMatched;//stores number of matched tiles
    private int mScore;//stores overall score
    private Tile mCardTurned1;//stores the turned over cards for value checking
    private Tile mCardTurned2;
    private String mScoreBoardName;//stores the name of the scoreboard to write to
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_window);//initialise all required values
        mTurned=0;
        mMatched=0;
        mScore=0;
        mCardTurned1=null;
        mCardTurned2=null;
        mScoreBoardName="";
        mCards=new ArrayList<>();
        if(getSupportFragmentManager().getFragments().size()==0) {//if the fragments have not already been created create the card fragments
            for (int i = 0; i < 40; i++) {
                FragmentTransaction initializer = getSupportFragmentManager().beginTransaction();
                imageFragment image = imageFragment.newInstance();
                initializer.add(R.id.CardContainer, image, String.valueOf(i));
                initializer.commitNow();
            }
        }

    }

    @Override
    protected void onStart() {//when everything is initialised
        super.onStart();
        if(mCards.size()==0) {//if the cards are not already initialised
            for (int i = 0; i < 40; i++) {//make a set of tiles with each tile holding pointers to a fragments images
                Fragment imageInitialised = getSupportFragmentManager().findFragmentByTag(String.valueOf(i));
                mCards.add(new Tile(imageInitialised.getView().findViewById(R.id.CardFront), imageInitialised.getView().findViewById(R.id.CardBack), i));
                mCards.get(i).getBottomImageView().setContentDescription(String.valueOf(i));
            }
            Bundle getExtras = getIntent().getExtras();
            if (getExtras != null) {//checks to see if we need to load a local card set
                if (getExtras.containsKey("cardSet")) {//if we do and there actually is one
                    String jsonFileName = getExtras.getString("cardSet");
                    mScoreBoardName = jsonFileName;//store jsonFileName as score board name
                    ArrayList<Bitmap> allImages = new ArrayList<>();//initialise local variables
                    Bitmap tileBack = BitmapFactory.decodeResource(getResources(), R.drawable.tileback_g);
                    ArrayList<String> allImageLocations = new ArrayList<>();
                    String tileBackFileLocation = "";
                    try {
                        InputStream is = getApplicationContext().openFileInput(jsonFileName);//load json object
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        String jsonInput = new String(buffer, "UTF-8");
                        JSONObject obj = new JSONObject(jsonInput);
                        tileBackFileLocation = obj.getString("TileBack").split("/")[1];//find tile names from the file
                        JSONArray jsonCards = obj.getJSONArray("PictureSet");
                        for (int i = 0; i < jsonCards.length(); i++) {
                            allImageLocations.add(jsonCards.getString(i).split("/")[1]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    File directory = getApplicationContext().getDir("cardImages", Context.MODE_PRIVATE);//open card image directory
                    File file = new File(directory, tileBackFileLocation);//load in the back tile from given location
                    if (file.exists()) {
                        try {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            tileBack = BitmapFactory.decodeStream(fileInputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    for (String cardLocation : allImageLocations) {//loads in all image tiles from their given location
                        directory = getApplicationContext().getDir("cardImages", Context.MODE_PRIVATE);
                        file = new File(directory, cardLocation);
                        if (file.exists()) {
                            try {
                                FileInputStream fileInputStream = new FileInputStream(file);
                                Bitmap toAdd = BitmapFactory.decodeStream(fileInputStream);
                                allImages.add(toAdd);
                                allImages.add(toAdd);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Random randomizer = new Random();//randomly assigns images across the board for matching
                    for (int i = 0; i < mCards.size(); i++) {
                        mCards.get(i).setBottomBitmap(tileBack);
                        mCards.get(i).getBottomImageView().setImageBitmap(mCards.get(i).getBottomBitmap());
                        int randomResult = randomizer.nextInt(allImages.size());
                        Bitmap randomGrab = allImages.get(randomResult);
                        mCards.get(i).setTopBitmap(randomGrab);
                        mCards.get(i).getTopImageView().setImageBitmap(mCards.get(i).getTopBitmap());
                        allImages.remove(randomResult);
                    }
                }
                else//if the extra is not passed in as designed warn the user (this code should in theory never trigger)
                {
                    Toast.makeText(getApplicationContext(),"Unrecognised card set!",Toast.LENGTH_SHORT).show();
                }
            } else {//if we are in test mode
                mScoreBoardName = "TestScoreBoard";
                for (int i = 0; i < mCards.size() / 2; i++)//adds top and bottom view to each card
                {
                    mCards.get(i).setTopBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.goldfish1));
                    mCards.get(i).setBottomBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tileback_g));
                }
                for (int i = 20; i < mCards.size(); i++)//temporary to test card matching
                {
                    mCards.get(i).setTopBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.goldfish2));
                    mCards.get(i).setBottomBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tileback_g));
                    mCards.get(i).getTopImageView().setImageBitmap(mCards.get(i).getTopBitmap());
                }
            }
        }
    }

    public void imageClicked(View view) {
        for(Tile card : mCards)//when an image is clicked loop through the cards to find the card that was clicked
        {
            if(view.getContentDescription()==String.valueOf(card.getID()))
            {
                if(!card.getIsFlipped())//if it is not already flipped flip it
                {
                    Animator FlipIn=AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_in);
                    Animator FlipOut=AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_out);
                    FlipIn.setTarget(card.getTopImageView());
                    FlipOut.setTarget(card.getBottomImageView());
                    FlipIn.start();
                    FlipOut.start();
                    card.setIsFlipped(true);
                    mTurned++;
                    if(mCardTurned1==null){
                        mCardTurned1=card;
                    }
                    else{
                        mCardTurned2=card;
                    }
                }
                break;
            }
        }
        if(mTurned==2)//if two cards are now flipped
        {
            mTurned=0;//reset flip counter
            mScore++;//increment score
            if(tileMatch(mCardTurned1,mCardTurned2))//check if tiles match
            {
                mMatched++;//play match animation and remove click events
                Animator mSpin=AnimatorInflater.loadAnimator(this,R.animator.spin_tile);
                mSpin.setTarget(mCardTurned1.getTopImageView());
                mSpin.setStartDelay(520);
                Animator mSpin2=AnimatorInflater.loadAnimator(this,R.animator.spin_tile);
                mSpin2.setTarget(mCardTurned2.getTopImageView());
                mSpin2.setStartDelay(520);
                mSpin.start();
                mSpin2.start();
                mCardTurned1.getBottomImageView().setOnClickListener(null);
                mCardTurned2.getBottomImageView().setOnClickListener(null);
            }
            else //if the cards do not match
            {
                Animator flipOut=AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_out);//turn cards back over and keep going
                Animator flipIn=AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_in);
                flipOut.setTarget(mCardTurned1.getTopImageView());
                flipIn.setTarget(mCardTurned1.getBottomImageView());
                flipOut.setStartDelay(520);
                flipIn.setStartDelay(520);
                flipOut.start();
                flipIn.start();
                mCardTurned1.setIsFlipped(false);
                Animator flipOut2= AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_out);
                Animator flipIn2= AnimatorInflater.loadAnimator(this,R.animator.flip_horizontal_in);
                flipOut2.setTarget(mCardTurned2.getTopImageView());
                flipIn2.setTarget(mCardTurned2.getBottomImageView());
                flipOut2.setStartDelay(520);
                flipIn2.setStartDelay(520);
                flipOut2.start();
                flipIn2.start();
                mCardTurned2.setIsFlipped(false);
            }
            mCardTurned1=null;//reset card turned values
            mCardTurned2=null;
        }
        if(mMatched==mCards.size()/2)//if all cards are matched
        {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("scores", MODE_PRIVATE);//Checks to see if a score has been set and if the users is higher than the set score award them the high score
            int highScore = settings.getInt(mScoreBoardName, Integer.MAX_VALUE);//gets value of score if there is no value set int to maximum so the score will always be a high score
            if (mScore < highScore) {//if you got a high score congratulate the user and save the score
                Toast.makeText(getApplicationContext(), "You Win with a new high score of: " + mScore + "!", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(mScoreBoardName, mScore);
                editor.apply();
            }
            else//if it is not high score congratulate user but not tell them new high score
            {
                Toast.makeText(getApplicationContext(), "You Win with score: " + mScore + "!", Toast.LENGTH_LONG).show();
            }
            finish();//close activity
        }
    }
    public boolean tileMatch(Tile pTile1,Tile pTile2) {
        return pTile1.getTopBitmap().sameAs(pTile2.getTopBitmap());//checks if top views of cards are the same
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {//on flip save instance info
        super.onSaveInstanceState(outState);
        outState.putInt("turned",mTurned);
        outState.putInt("matched",mMatched);
        outState.putInt("score",mScore);
        outState.putParcelable("CardTurned1",mCardTurned1);
        outState.putParcelable("CardTurned2",mCardTurned2);
        outState.putString("scoreBoard",mScoreBoardName);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {//on flip complete reload instance info
        super.onRestoreInstanceState(savedInstanceState);
        mTurned=savedInstanceState.getInt("turned");
        mMatched=savedInstanceState.getInt("matched");
        mScore=savedInstanceState.getInt("score");
        mCardTurned1=savedInstanceState.getParcelable("CardTurned1");
        mCardTurned2=savedInstanceState.getParcelable("CardTurned2");
        mScoreBoardName=savedInstanceState.getString("scoreBoard");
    }

}