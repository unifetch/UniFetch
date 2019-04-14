package com.jay.unidrive.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseUser;

public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int iconPicture;

    public ClusterMarker(LatLng position, String title, String snippet, int iconPicture) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
    }

    public ClusterMarker (){

    }

    @Override
    public LatLng getPosition() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
