package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;

// import com.synnapps.carouselview.CarouselView;
// import com.synnapps.carouselview.ImageListener;

/**
 * Created by tog on 21/10/2018.
 */

public class CustomMarkerDialog  implements GoogleMap.InfoWindowAdapter {
    private Context context;
    // private CarouselView carouselView;
    private int[] sampleImages = {};

    public MainActivity getMain () {
        return (MainActivity) context;
    }

    public CustomMarkerDialog (Context context) {
        this.context = context;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.marker_dialog_layout, null);

        ImageView image = (ImageView) view.findViewById(R.id.imageView);
        image.setImageResource(R.drawable.logo);

        /*
        // ajouter ici diapo
        carouselView = (CarouselView) view.findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);
        carouselView.setImageListener(imageListener);
        */

        /*
        CarouselPicker carouselPicker = (CarouselPicker) view.findViewById(R.id.carousel);

        // Case 1 : To populate the picker with images
        List<CarouselPicker.PickerItem> imageItems = new ArrayList<>();
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.logo));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.logo));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.logo));
        //Create an adapter
        CarouselPicker.CarouselViewAdapter imageAdapter = new CarouselPicker.CarouselViewAdapter(context, imageItems, 0);
        //Set the adapter
        carouselPicker.setAdapter(imageAdapter);
        */

        return view;
    }

    /*
    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(sampleImages[position]);
        }
    };
    */
}
