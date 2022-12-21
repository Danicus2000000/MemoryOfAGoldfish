package Bulman.Daniel.MemoryOfAGoldfish;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class RemoteTileGenerator {
    private static RemoteTileGenerator sTileGenerator;//stores instance of itself
    private final Context mApplicationContext;//stores context this generator is used in
    private final MediatorLiveData<ArrayList<Tile>> mTiles;//stores tile list
    private LiveData<Tile> mSelectedTile;//stores the selected tile
    private final String mUrl;//stores url for downloading from
    private final String mFileName;//stores name of .json file to be used for saving
    private RemoteTileGenerator (Context pApplicationContext,String pUrl)//constructor for remote tile generator
    {
        mApplicationContext=pApplicationContext;
        mTiles=new MediatorLiveData<>();
        mUrl=pUrl;
        String[] fileSplit=pUrl.split("/");
        mFileName=fileSplit[fileSplit.length-2]+fileSplit[fileSplit.length-1];
    }
    public static RemoteTileGenerator getInstance(Context pApplicationContext,String pUrl){//generates itself
        sTileGenerator=new RemoteTileGenerator(pApplicationContext,pUrl);
        return sTileGenerator;
    }
    public void saveImageClusterLocal(JSONObject pIndexObject,String pFileName)//saves .json image pointer locally
    {
        ContextWrapper contextWrapper=new ContextWrapper(mApplicationContext);
        try
        {
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(contextWrapper.openFileOutput(pFileName,Context.MODE_PRIVATE));
            outputStreamWriter.write(pIndexObject.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    public boolean loadImageLocally(String pFilename,MutableLiveData<Tile> pTileData)//loads image locally
    {
        boolean loaded=false;
        ContextWrapper contextWrapper=new ContextWrapper(mApplicationContext);
        File directory=contextWrapper.getDir("cardImages",Context.MODE_PRIVATE);
        File file=new File(directory,pFilename);
        if(file.exists()){
            try{
                FileInputStream fileInputStream=new FileInputStream(file);
                Bitmap bitmap= BitmapFactory.decodeStream(fileInputStream);
                Tile tile =pTileData.getValue();
                tile.setTopBitmap(bitmap);
                pTileData.setValue(tile);
                fileInputStream.close();
                loaded=true;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return loaded;
    }
    public void saveImageLocally(Bitmap pBitmap,String pFilename){//saves image locally
        ContextWrapper contextWrapper=new ContextWrapper(mApplicationContext);
        File directory=contextWrapper.getDir("cardImages",Context.MODE_PRIVATE);
        File file=new File(directory,pFilename);
        if(!file.exists()){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                pBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public LiveData<ArrayList<Tile>> loadTilesFromJSON()//loads tiles from downloaded json file
    {
        RequestQueue queue= Volley.newRequestQueue(mApplicationContext);
        final MutableLiveData<ArrayList<Tile>> mutableTiles=new MutableLiveData<>();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(
                Request.Method.GET,
                mUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        saveImageClusterLocal(response, mFileName);
                        ArrayList<Tile> tiles = parseJSONResponse(response);
                        mutableTiles.setValue(tiles);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mApplicationContext,"Error: Invalid URL!",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(jsonObjectRequest);
        return mutableTiles;
    }
    private ArrayList<Tile> parseJSONResponse(JSONObject pResponse)//parses inbound json response from download
    {
        ArrayList<Tile> tiles=new ArrayList<>();
        try{
            JSONArray frontImages=pResponse.getJSONArray("PictureSet");
            String backImage=pResponse.getString("TileBack");
            loadImage(backImage);
            for(int i=0;i<frontImages.length();i++){
                String frontImage=frontImages.getString(i);
                MutableLiveData<Tile> tile=new MutableLiveData<>();
                tile.setValue(new Tile(frontImage,backImage));
                loadImage(frontImage);
                tiles.add(tile.getValue());
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return tiles;
    }
    public void getTile(int pIndex)//gets specific tile image
    {
        LiveData<Tile> transformedTile= Transformations.switchMap(mTiles,tiles->{
            MutableLiveData<Tile> tileData=new MutableLiveData<>();
            Tile tile=tiles.get(pIndex);
            tileData.setValue(tile);
            if(!loadImageLocally(Uri.parse(tile.getTopUrl()).getLastPathSegment(),tileData))
            {
                loadImage(tile.getTopUrl());
            }
            else if(!loadImageLocally(Uri.parse(tile.getBottomUrl()).getLastPathSegment(),tileData))
            {
                loadImage(tile.getBottomUrl());
            }
            return tileData;
        });
        mSelectedTile=transformedTile;
    }

    public void loadImage(String pUrl)//volleys for the image data to be used
    {
        RequestQueue queue=Volley.newRequestQueue(mApplicationContext);
        ImageRequest frontImageRequest=new ImageRequest(
                mUrl.replace(mUrl.split("/")[mUrl.split("/").length-1],pUrl), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                saveImageLocally(bitmap,Uri.parse(pUrl).getLastPathSegment());
            }
        },
                0, 0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mApplicationContext,error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(frontImageRequest);
    }
}
