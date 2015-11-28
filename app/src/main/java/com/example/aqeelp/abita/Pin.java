package com.example.aqeelp.abita;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aqeelp on 10/24/15.
 */
public class Pin {
    final private String[] PINTYPESTRINGS = { "Wildlife", "Foliage", "Scenery", "Architecture" };
    final private ArrayList<String> PINTYPES =
            new ArrayList<String>(Arrays.asList(PINTYPESTRINGS));
    final private int[] PINCOLORS = { Color.parseColor("#fffbbc05"),
            Color.parseColor("#ff34a853"), Color.parseColor("#ff2c59a3"),
            Color.parseColor("#ffea4335") };

    final private int pinId;
    final private int userId;
    final private String pinType;
    final private int pinTypeIndex;
    final private LatLng pinLocation;
    final private String pinTitle;
    final private MapsActivity parent;

    private Description[] pinDescriptions;
    private User pinUser;

    private Bitmap thumbnail;
    private Bitmap fullSize;

    /**
     * Images:
     * - get icon bitmap upon retrieving pin
     * - get full bitmap when in close enough range to
     */

    public Pin(int pid, int uid, String ptype, LatLng loc, String ti, MapsActivity p) {
        pinId = pid;
        userId = uid;
        pinType = ptype;
        pinTypeIndex = PINTYPES.indexOf(pinType);
        pinLocation = loc;
        pinTitle = ti;
        parent = p;
    }

    public void show(GoogleMap map) {
        map.setOnMarkerClickListener(PinClickListener); // Set custom marker click listener

        Bitmap[] pinIcons = {
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.wildlife_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.foliage_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landscape_pin),
                BitmapFactory.decodeResource(parent.getResources(), R.drawable.landmark_pin)
        };

        Bitmap fullSizePin = pinIcons[pinTypeIndex];
        Bitmap smallPin = Bitmap.createScaledBitmap(fullSizePin, 150, 150, false);

        map.addMarker(new MarkerOptions()
                .position(pinLocation)
                .title(pinId + "")
                .icon(BitmapDescriptorFactory.fromBitmap(smallPin)));
    }

    public void fetchDescriptions() {
        DescriptionRetrieval descGetter = new DescriptionRetrieval((Pin) this);
        descGetter.execute("https://www.abitatech.net:5000/api/pins/26/descriptions");
    }

    public void setPinDescriptions(Description[] descriptions) {
        pinDescriptions = new Description[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            // Make a defensive copy of all descriptions?
            pinDescriptions[i] = descriptions[i];
        }
    }

    public void fetchUser() {
        UserRetrieval userGetter = new UserRetrieval((Pin) this);
        userGetter.execute("https://www.abitatech.net:5000/api/users/"+userId);
    }

    public void setPinUser(User user) {
        // Defensive copy? probably not
        this.pinUser = user;
        parent.addNewUser(user);
    }

    /**
     * Getter and setter methods follow:
     */
    public String getPinType() {
        return pinType;
    }

    public int getPinTypeIndex() {
        return pinTypeIndex;
    }

    public int getPinId() {
        return pinId;
    }

    public int getUserId() {
        return userId;
    }

    public LatLng getPinLocation() {
        return pinLocation;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public Description[] getPinDescriptions() {
        return pinDescriptions;
    }

    public String toString() {
        return "Pin! Title: " + getPinTitle() + " Location: " + getPinLocation();
    }

    public MapsActivity getParent() {
        return parent;
    }

    /**
     * Show a pin's detail when it is tapped
     */
    private GoogleMap.OnMarkerClickListener PinClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // Resolve the pin object from its ID
            Pin pin = parent.findPinById(Integer.parseInt(marker.getTitle()));

            if (pin != null) {
                // Inflate the pin detail
                Dialog dialog = new Dialog(parent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                // Set the layout view of the dialog
                dialog.setContentView(R.layout.pin_info);

                // Set title (default previous disabled)
                TextView titleView = (TextView) dialog.findViewById(R.id.PinInfoTitle);
                String title = pin.getPinTitle() + " - " + PINTYPES.get(pin.getPinTypeIndex());
                titleView.setText(title);

                View header = dialog.findViewById(R.id.PinInfoHeader);
                View readMore = dialog.findViewById(R.id.PinInfoReadMore);
                header.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);
                readMore.setBackgroundColor(PINCOLORS[pin.getPinTypeIndex()]);

                //TODO: fill section wtih descriptions
                LinearLayout descriptionSection = (LinearLayout)
                        dialog.findViewById(R.id.pin_description_section);
                Description[] descs = pin.getPinDescriptions();
                for (int i = 0; i < descs.length; i++) {
                    Context context = pin.getParent().getApplicationContext();
                    DescriptionView descriptionView = new DescriptionView(context, descs[i]);
                    descriptionSection.addView(descriptionView.getView());
                }

                // Force size
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.show();
            }
            return true;
        }
    };
}
