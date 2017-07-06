package com.clementlan.pebblefy;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by Clement Lan on 7/6/2017.
 * Custom TextView which also stores Spotify playlist data
 */

public class PlaylistTextView extends android.support.v7.widget.AppCompatTextView {

    private PlaylistSimple mPlaylist;

    public PlaylistTextView(Context context) {
        super(context);
    }

    public PlaylistTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaylistTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPlaylist(PlaylistSimple playlist) {
        this.mPlaylist = playlist;
    }

    public String getPlaylistURI() {
        return this.mPlaylist.uri;
    }
}
