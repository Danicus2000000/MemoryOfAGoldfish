package Bulman.Daniel.MemoryOfAGoldfish;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardSetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardSetFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";//argument for the fragments title
    private static final String ARG_HIGH_SCORE="highScore";//argument for the highScore for this card set
    private String mHighScore;//stores high score
    private String mTitle;//stores title
    public CardSetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pTitle Text for text view.
     * @param pHighScore text for highscore
     * @return A new instance of fragment CardSetFragment.
     */
    public static CardSetFragment newInstance(String pTitle,String pHighScore)//gets fragment arguments
    {
        CardSetFragment fragment = new CardSetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, pTitle);
        args.putString(ARG_HIGH_SCORE,pHighScore);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)//sets fragment arguments
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mHighScore=getArguments().getString(ARG_HIGH_SCORE);
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)//assigns arguments to views
    {
        //stores the inflated view
        View InflatedView = inflater.inflate(R.layout.fragment_card_set, container, false);
        TextView title= InflatedView.findViewById(R.id.CardSetTitle);
        TextView highScore= InflatedView.findViewById(R.id.CardSetHighScoreContainer);
        highScore.setText(mHighScore);
        title.setText(mTitle);
        return InflatedView;
    }
}