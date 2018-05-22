package br.com.leoassuncao.bakingapp.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import br.com.leoassuncao.bakingapp.R;
import br.com.leoassuncao.bakingapp.activities.RecipeDetailActivity;
import br.com.leoassuncao.bakingapp.pojo.Recipe;
import br.com.leoassuncao.bakingapp.pojo.Step;

import static br.com.leoassuncao.bakingapp.activities.RecipeActivity.SELECTED_RECIPES;
import static br.com.leoassuncao.bakingapp.activities.RecipeActivity.SELECTED_INDEX;
import static br.com.leoassuncao.bakingapp.activities.RecipeDetailActivity.SELECTED_POSITION;
import static br.com.leoassuncao.bakingapp.activities.RecipeActivity.SELECTED_STEPS;

/**
 * Created by leonardo.filho on 14/05/2018.
 */

public class RecipeStepDetailFragment extends Fragment {
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private BandwidthMeter bandwidthMeter;
    private ArrayList<Step> steps = new ArrayList<>();
    private int selectedIndex;
    private Handler mainHandler;
    ArrayList<Recipe> recipe;
    String recipeName;
    Long position;
    Uri videoUri;
    private static final String INSTANCE_STATE_TITLE = "Title";


    public RecipeStepDetailFragment() {

    }

    private ListItemClickListener itemClickListener;

    public interface ListItemClickListener {
        void onListItemClick(List<Step> allSteps, int Index, String recipeName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView;
        mainHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter();

        itemClickListener =(RecipeDetailActivity)getActivity();

        recipe = new ArrayList<>();
        position = C.TIME_UNSET;
        if(savedInstanceState != null) {
            steps = savedInstanceState.getParcelableArrayList(SELECTED_STEPS);
            selectedIndex = savedInstanceState.getInt(SELECTED_INDEX);
            recipeName = savedInstanceState.getString(INSTANCE_STATE_TITLE);
            position = savedInstanceState.getLong(SELECTED_POSITION, C.TIME_UNSET);
        }
        else {
            steps =getArguments().getParcelableArrayList(SELECTED_STEPS);
            if (steps!=null) {
                steps =getArguments().getParcelableArrayList(SELECTED_STEPS);
                selectedIndex=getArguments().getInt(SELECTED_INDEX);
                recipeName=getArguments().getString(INSTANCE_STATE_TITLE);
            }
            else {
                recipe =getArguments().getParcelableArrayList(SELECTED_RECIPES);
                //casting List to ArrayList
                steps=(ArrayList<Step>)recipe.get(0).getSteps();
                selectedIndex=0;
            }

        }

        View rootView = inflater.inflate(R.layout.fragment_step_detail, container, false);
        textView = (TextView) rootView.findViewById(R.id.recipe_step_detail_text);
        textView.setText(steps.get(selectedIndex).getDescription());
        textView.setVisibility(View.VISIBLE);

        simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.playerView);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);



        if (rootView.findViewWithTag("sw600dp-port-recipe_step_detail")!=null) {
            recipeName=((RecipeDetailActivity) getActivity()).recipeName;
            ((RecipeDetailActivity) getActivity()).getSupportActionBar().setTitle(recipeName);
        }

        String imageUrl=steps.get(selectedIndex).getThumbUrl();
        if (imageUrl!= null) {
            Uri builtUri = Uri.parse(imageUrl).buildUpon().build();
            ImageView thumbImage = (ImageView) rootView.findViewById(R.id.thumbImage);
            Picasso.with(getContext()).load(builtUri).into(thumbImage);
        }

        String videoURL = steps.get(selectedIndex).getVideoUrl();
         videoUri = Uri.parse(steps.get(selectedIndex).getVideoUrl());

        if (!videoURL.isEmpty()) {

            initializePlayer(videoUri);

            if (rootView.findViewWithTag("sw600dp-land-recipe_step_detail")!=null) {
                getActivity().findViewById(R.id.fragment_container2).setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            }
            else if (isInLandscapeMode(getContext())){
                textView.setVisibility(View.GONE);
            }
        }
        else {
            player=null;
            simpleExoPlayerView.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.ic_video_off_white_48dp));
            simpleExoPlayerView.setLayoutParams(new LinearLayout.LayoutParams(500, 300));
        }

        Button mPrevStep = (Button) rootView.findViewById(R.id.previousStep);
        Button mNextstep = (Button) rootView.findViewById(R.id.nextStep);

        mPrevStep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (steps.get(selectedIndex).getId() > 0) {
                    if (player!=null){
                        player.stop();
                    }
                    itemClickListener.onListItemClick(steps,steps.get(selectedIndex).getId() - 1,recipeName);
                }
                else {
                    Toast.makeText(getActivity(),getResources().getString(R.string.first_recipe), Toast.LENGTH_SHORT).show();

                }
            }});

        mNextstep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                int lastIndex = steps.size()-1;
                if (steps.get(selectedIndex).getId() < steps.get(lastIndex).getId()) {
                    if (player!=null){
                        player.stop();
                    }
                    itemClickListener.onListItemClick(steps,steps.get(selectedIndex).getId() + 1,recipeName);
                }
                else {
                    Toast.makeText(getContext(),getResources().getString(R.string.last_recipe), Toast.LENGTH_SHORT).show();

                }
            }});

        return rootView;
    }

    private void initializePlayer(Uri mediaUri) {
        if (player == null) {
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();

            player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            simpleExoPlayerView.setPlayer(player);
            if (position != C.TIME_UNSET) player.seekTo(position);
            String userAgent = Util.getUserAgent(getContext(), "Baking App");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onSaveInstanceState( Bundle currentState) {
        super.onSaveInstanceState(currentState);
        currentState.putParcelableArrayList(SELECTED_STEPS,steps);
        currentState.putInt(SELECTED_INDEX,selectedIndex);
        currentState.putString("Title",recipeName);
        currentState.putLong(SELECTED_POSITION, position);
    }

    public boolean isInLandscapeMode( Context context ) {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }




    @Override
    public void onPause() {
        super.onPause();
        if (player!=null) {
            position = player.getCurrentPosition();
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoUri != null)
            initializePlayer(videoUri);
    }

}
