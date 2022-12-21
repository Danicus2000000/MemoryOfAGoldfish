package Bulman.Daniel.MemoryOfAGoldfish;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class Tile implements Parcelable {
    private Bitmap mTopBitmap;//required storage
    private Bitmap mBottomBitmap;
    private boolean mIsFlipped;
    private ImageView mTopImageView;
    private ImageView mBottomImageView;
    private String mTopUrl;
    private String mBottomUrl;
    private Integer mID;

    protected Tile(Parcel in) {
        mTopImageView = in.readParcelable(Bitmap.class.getClassLoader());
        mBottomImageView = in.readParcelable(Bitmap.class.getClassLoader());
        mIsFlipped = in.readByte() != 0;
        mTopUrl = in.readString();
        mBottomUrl = in.readString();
        if (in.readByte() == 0) {
            mID = null;
        } else {
            mID = in.readInt();
        }
    }

    public static final Creator<Tile> CREATOR = new Creator<Tile>() {
        @Override
        public Tile createFromParcel(Parcel in) {
            return new Tile(in);
        }

        @Override
        public Tile[] newArray(int size) {
            return new Tile[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(mTopBitmap, flags);
        dest.writeParcelable(mBottomBitmap, flags);
        dest.writeByte((byte) (mIsFlipped ? 1 : 0));
        dest.writeString(mTopUrl);
        dest.writeString(mBottomUrl);
        if (mID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(mID);
        }
    }

    public Tile(ImageView pTopImageView, ImageView pBottomImageView, Integer pID)//constructor sets flipped default value and sets image view for tile
    {
        setTopImageView(pTopImageView);
        setBottomImageView(pBottomImageView);
        setID(pID);
        setIsFlipped(false);
    }
    public Tile(String pTopUrl,String pBottomUrl)
    {
        setTopUrl(pTopUrl);
        setBottomUrl(pBottomUrl);
        setIsFlipped(false);
    }
    public Bitmap getTopBitmap(){return mTopBitmap;}//getters and setters
    public void setTopBitmap(Bitmap pTopBitmap){mTopBitmap=pTopBitmap;}
    public Bitmap getBottomBitmap(){return mBottomBitmap;}
    public void setBottomBitmap(Bitmap pBottomBitmap){mBottomBitmap=pBottomBitmap;}
    public Boolean getIsFlipped(){return mIsFlipped;}
    public void setIsFlipped(Boolean pIsFlipped){mIsFlipped=pIsFlipped;}
    public ImageView getTopImageView(){return mTopImageView;}
    public void setTopImageView(ImageView pTopImageView){mTopImageView=pTopImageView;}
    public ImageView getBottomImageView(){return mBottomImageView;}
    public void setBottomImageView(ImageView pBottomImageView){mBottomImageView=pBottomImageView;}
    public String getTopUrl(){return mTopUrl;}
    public void setTopUrl(String pTopUrl){mTopUrl=pTopUrl;}
    public String getBottomUrl(){return mBottomUrl;}
    public void setBottomUrl(String pBottomUrl){mBottomUrl=pBottomUrl;}
    public Integer getID(){return mID;}
    public void setID(Integer pID){mID=pID;}
}
