package com.hutchsystems.hutchconnect.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.fragments.NewEventFragment;

import java.util.List;


public class EventAdapter extends ArrayAdapter<EventBean> {
    private ItemClickListener mListener;

    private final Context context;

    private List<EventBean> listEvents;

    View selectedView = null;
    ViewHolder selectedHolder = null;
    boolean fromInspect = false;

    static class ViewHolder {
        public TextView tvTrip;
        public TextView tvShipStatus, tvShipment;
        public TextView tvLocation;
        public TextView tvComments, tvEventStatus;
        public TextView tvTime, tvDuration;
        public Button butEventIcon;
        public ImageButton butEditEvent;
        public LinearLayout infoLayout, layoutInformation;
        public ImageView imgShipmentIcon, imgLocationIcon, imgSplitSleepIcon, imgWaiting;
    }

    boolean eventDetailFg = false;

    public EventAdapter(Context context, List<EventBean> values) {
        super(context, -1, values);
        this.mListener = (ItemClickListener) context;
        this.context = context;
        this.listEvents = values;
        eventDetailFg = Utility.getPreferences("event_detail", false);
    }

    public void setFromInspect(boolean value) {
        fromInspect = value;
    }

    public void changeItems(List<EventBean> list) {
        listEvents.clear();
        listEvents.addAll(list);

        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.event_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvTrip = (TextView) rowView.findViewById(R.id.tvTrip);

                viewHolder.tvShipStatus = (TextView) rowView.findViewById(R.id.tvShipStatus);
                viewHolder.tvShipment = (TextView) rowView.findViewById(R.id.tvShipment);
                viewHolder.tvLocation = (TextView) rowView.findViewById(R.id.tvLocation);
                viewHolder.tvLocation.setSelected(true);
                viewHolder.tvTime = (TextView) rowView.findViewById(R.id.tvTime);
                viewHolder.tvComments = (TextView) rowView.findViewById(R.id.tvComments);
                viewHolder.tvComments.setVisibility(View.INVISIBLE);
                viewHolder.butEventIcon = (Button) rowView.findViewById(R.id.butEventIcon);

                viewHolder.butEditEvent = (ImageButton) rowView.findViewById(R.id.butEditEvent);
                viewHolder.butEditEvent.setVisibility(View.INVISIBLE);
                viewHolder.infoLayout = (LinearLayout) rowView.findViewById(R.id.layout_slide);
                viewHolder.layoutInformation = (LinearLayout) rowView.findViewById(R.id.layoutInformation);
                viewHolder.tvEventStatus = (TextView) rowView.findViewById(R.id.tvEventStatus);
                viewHolder.imgShipmentIcon = (ImageView) rowView.findViewById(R.id.imgShipmentIcon);
                viewHolder.imgLocationIcon = (ImageView) rowView.findViewById(R.id.imgLocationIcon);

                // Duration
                viewHolder.tvDuration = (TextView) rowView.findViewById(R.id.tvDuration);
                viewHolder.imgSplitSleepIcon = (ImageView) rowView.findViewById(R.id.imgSplitSleepIcon);
                viewHolder.imgWaiting = (ImageView) rowView.findViewById(R.id.imgWaitingIcon);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();
            // fill data
            Resources resources = context.getResources();
            EventBean eventItem = listEvents.get(position);
            final int eventType = eventItem.getEventType();
            final int eventCode = eventItem.getEventCode();

            final String vTitle = eventItem.getViolationTitle();
            final String vExplanation = eventItem.getViolationExplanation();
            /*  if (!Utility.isLargeScreen(getContext().getApplicationContext())) {*/
            float distanceOut = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1,
                    getContext().getResources().getDisplayMetrics()
            );
            holder.infoLayout.animate().translationX(distanceOut).setDuration(1).start();
            holder.infoLayout.bringToFront();
            holder.butEditEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.i("Event", "button Edit clicked");
                    if (mListener != null) {
                        mListener.onEventEdited();
                    }
                }
            });
            /*  }*/

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.InspectorModeFg)
                        return;

                    if (eventType == -1) {
                        try {
                            final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                            alertDialog.setCancelable(true);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setTitle(vTitle);
                            alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                            alertDialog.setMessage(vExplanation);
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, Utility.context.getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.cancel();
                                        }
                                    });
                            alertDialog.show();
                        } catch (Exception ex) {
                            LogFile.write("onViolationClick Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                        }
                        return;
                    }

                  /*  if (eventType == 1 && eventCode == 3 && !ConstantFlag.Flag_DRIVING_EDIT)
                        return;*/

                    if (eventCode == 0)
                        return;
                    //Log.i("Event", "row clicked");
                    mListener.editEvent(listEvents.get(position));
                    NewEventFragment.CurrentEventFg = (position == 0);
                    /* if (!Utility.isLargeScreen(getContext().getApplicationContext())) {*/
                    if (eventType <= 3) {
                        if (listEvents.get(position).getEventRecordStatus() == 1) {

                            float distanceIn = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 70,
                                    getContext().getResources().getDisplayMetrics()
                            );
                            float distanceOut = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, -1,
                                    getContext().getResources().getDisplayMetrics()
                            );

                            if (selectedHolder != null) {
                                //unselect
                                selectedHolder.infoLayout.animate().translationX(distanceOut).setDuration(500).start();
                            }
                            selectedHolder = holder;

                            if (selectedView != v) {
                                holder.butEditEvent.setVisibility(View.VISIBLE);
                                holder.infoLayout.animate().translationX(distanceIn).setDuration(500).start();
                                selectedView = v;
                            } else {
                                holder.infoLayout.animate().translationX(distanceOut).setDuration(500).start();
                                //holder.butEditEvent.setVisibility(View.INVISIBLE);
                                selectedView = null;
                            }
                        }
                    }
                }
            });

            String shipStatus = context.getString(R.string.at);
            if (eventCode == 3) {
                shipStatus = context.getString(R.string.departed);
            } else if (eventCode == 4) {
                shipStatus = context.getString(R.string.arrived_at);
            }

            double odometerReading = 0d, accumulatedMiles = 0d;
            if (eventItem.getOdometerReading() != null)
                odometerReading = Double.valueOf(eventItem.getOdometerReading()); // odometer from can bus is in km

            if (eventItem.getAccumulatedVehicleMiles() != null)
                accumulatedMiles = Double.valueOf(eventItem.getAccumulatedVehicleMiles()); // odometer from can bus is in km

            String unit = context.getString(R.string.kms);

            if (Utility._appSetting.getUnit() == 2) {
                odometerReading = odometerReading * .62137d;
                accumulatedMiles = accumulatedMiles * .62137d;
                unit = " mi"; //context.getString(R.string.miles);
            }

            String trip = "Odo: " + Double.valueOf(odometerReading).intValue()+ unit + ", Eng. Hrs: " + eventItem.getEngineHour();


            holder.tvTrip.setText(trip);

            // hide if event detail fg is false
            holder.tvTrip.setVisibility(eventDetailFg ? View.VISIBLE : View.GONE);

            String location = eventItem.getLocationDescription();
            if (location == null) {
                location = "";
            }

            String shipment = eventItem.getShippingDocumentNo();
            if (shipment == null) {
                shipment = "";
            }

            holder.imgShipmentIcon.setVisibility(View.VISIBLE);
            holder.imgLocationIcon.setVisibility(View.VISIBLE);
            holder.tvShipment.setVisibility(View.VISIBLE);
            holder.tvShipStatus.setVisibility(View.VISIBLE);
            holder.tvLocation.setVisibility(View.VISIBLE);

            if (!shipment.isEmpty() && !location.isEmpty()) {
                holder.tvShipment.setText(shipment);
                holder.tvShipStatus.setText(shipStatus);
                holder.tvLocation.setText(location);

            } else if (!location.isEmpty()) {
                holder.imgShipmentIcon.setVisibility(View.GONE);
                holder.tvShipment.setVisibility(View.GONE);
                holder.tvShipStatus.setVisibility(View.GONE);
                holder.tvLocation.setText(location);
            } else if (!shipment.isEmpty()) {
                holder.imgLocationIcon.setVisibility(View.GONE);
                holder.tvLocation.setVisibility(View.GONE);
                holder.tvShipStatus.setVisibility(View.GONE);
                holder.tvShipment.setText(shipment);
            } else {
                holder.imgShipmentIcon.setVisibility(View.GONE);
                holder.imgLocationIcon.setVisibility(View.GONE);
                holder.tvShipment.setVisibility(View.GONE);
                holder.tvShipStatus.setVisibility(View.GONE);
                holder.tvLocation.setVisibility(View.GONE);
            }

            holder.imgShipmentIcon.setVisibility(View.GONE);
            // hide shipment detail if event detail is false
            if (!eventDetailFg) {
                holder.tvShipment.setVisibility(View.GONE);
                holder.tvShipStatus.setVisibility(View.GONE);
            }

            String comments = eventItem.getAnnotation();
            if (comments != null && !comments.isEmpty()) {
                comments = "\"" + comments + "\"";
                holder.tvComments.setText(comments);
                holder.tvComments.setVisibility(View.VISIBLE);
            } else
                holder.tvComments.setVisibility(View.GONE);
            /*}*/


            String dateTime = Utility.getTimeByFormat(listEvents.get(position).getEventDateTime());


            if (listEvents.get(position).getEventRecordOrigin() == 1) {
                //holder.imgOrigin.setImageResource(R.drawable.ic_stay_current_portrait_black_24dp);
                holder.tvEventStatus.setText("Auto");
            } else {
                //holder.imgOrigin.setImageResource(R.drawable.ic_account_box_black_24dp);
                holder.tvEventStatus.setText("Driver");
            }

            //holder.layoutInformation.setBackgroundResource(R.color.d3d3d3);
            if (fromInspect && eventType > 0) {
                int status = listEvents.get(position).getEventRecordStatus();
                if (status == 1) {
                    holder.layoutInformation.setBackgroundResource(R.color.white);
                    holder.layoutInformation.setAlpha(1f);
                } else if (status == 3) {
                    //holder.layoutInformation.setAlpha(.5f);
                    holder.tvEventStatus.setAlpha(.7f);
                    holder.layoutInformation.setBackgroundResource(R.color.light_blue);
                } else {
                    holder.tvEventStatus.setAlpha(.7f);
                    holder.layoutInformation.setBackgroundResource(R.color.e5e5e5);

                }
            }

            // if event is certified log then show both date and time
            if (eventType==4)
            {
                String eventDate=Utility.getStringCurrentDate(listEvents.get(position).getEventDateTime());
                dateTime=eventDate+" "+dateTime;
            }

            holder.tvTime.setText(dateTime);
            // For Duration
            String duration = listEvents.get(position).getDuration();

            // show extra information only if event detail is true from setting
            if (eventDetailFg) {
                if (eventType <= 3) {
                    duration += " ACC: " + Double.valueOf(accumulatedMiles).intValue() + " " + unit + " Eng: " + listEvents.get(position).getElaspsedEngineHour();
                } else if (eventType == 7) {
                    duration = "Diagnostic Code: " + listEvents.get(position).getDiagnosticCode();
                }
            } else {

            }

            if (duration != null && eventType <= 3) {
                holder.tvDuration.setText("Dur: " + duration);
                holder.tvDuration.setVisibility(View.VISIBLE);
            } else if (eventType == 7) {

                holder.tvDuration.setText(duration);
                holder.tvDuration.setVisibility(View.VISIBLE);
            } else
                holder.tvDuration.setVisibility(View.GONE);

            // if split sleep is applicable for this event
            if (listEvents.get(position).getSplitSleep() == 1) {
                holder.imgSplitSleepIcon.setVisibility(View.VISIBLE);
            } else {
                holder.imgSplitSleepIcon.setVisibility(View.GONE);
            }

            // if driver is in waiting or stand by when in Oil well service
            if (listEvents.get(position).isWaitingFg()) {
                holder.imgWaiting.setVisibility(View.VISIBLE);
            } else {
                holder.imgWaiting.setVisibility(View.GONE);
            }


            String statusText = "";
            switch (eventType) {
                case -1:
                    statusText = context.getString(R.string.vl);
                    holder.tvTrip.setText(listEvents.get(position).getViolation());
                    holder.tvComments.setText(context.getString(R.string.total_hours) + listEvents.get(position).getViolationMintes());
                    holder.tvComments.setVisibility(View.VISIBLE);
                    holder.imgShipmentIcon.setVisibility(View.GONE);
                    holder.imgLocationIcon.setVisibility(View.GONE);
                    holder.tvTrip.setVisibility(View.VISIBLE);

                    break;
                case 1:
                    if (eventCode == 1)
                        statusText = resources.getString(R.string.off_duty);
                    else if (eventCode == 2)
                        statusText = resources.getString(R.string.sleeper);
                    else if (eventCode == 3)
                        statusText = resources.getString(R.string.driving);
                    else
                        statusText = resources.getString(R.string.on_duty);
                    break;
                case 2:
                    if (eventCode == 1) {
                        statusText = resources.getString(R.string.ILC);
                    } else {
                        statusText = resources.getString(R.string.ILR);
                    }
                    break;
                case 3:
                    if (eventCode == 1)
                        statusText = resources.getString(R.string.personal_use);
                    else if (eventCode == 2)
                        statusText = resources.getString(R.string.yard_move);
                    else {
                        statusText = context.getString(R.string.clr);
                    }
                    break;
                case 4:
                    statusText = resources.getString(R.string.certified_record);
                    break;
                case 5:
                    if (eventCode == 1)
                        statusText = resources.getString(R.string.login);
                    else if (eventCode == 2)
                        statusText = resources.getString(R.string.logout);
                    break;
                case 6:
                    if (eventCode == 1 || eventCode == 2)
                        statusText = resources.getString(R.string.engine_up);
                    else if (eventCode == 3 || eventCode == 4)
                        statusText = resources.getString(R.string.engine_down);
                    break;
                case 7:
                    if (eventCode == 1 || eventCode == 2)
                        statusText = resources.getString(eventCode == 1 ? R.string.malfunction : R.string.malfunction_clear);
                    else if (eventCode == 3 || eventCode == 4)
                        statusText = resources.getString(eventCode == 3 ? R.string.data_diagnostic : R.string.data_diagnostic_clear);
                    break;
            }

            holder.butEventIcon.setText(statusText);
        } catch (Exception e) {

            LogFile.write(EventAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventAdapter.class.getName(), "::getView Error:", e.getMessage(), Utility.printStackTrace(e));

        }

        return rowView;
    }

    public void unselectEvent() {
        //if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
        float distanceOut = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -63,
                getContext().getResources().getDisplayMetrics()
        );

        if (selectedHolder != null) {
            //unselect
            selectedHolder.infoLayout.animate().translationX(distanceOut).setDuration(500).start();
            selectedHolder = null;
        }

        /*} else {
            if (selectedView != null) {
                selectedView.setBackgroundResource(R.color.white);
                selectedView = null;
            }
        }*/
    }

    public interface ItemClickListener {
        void editEvent(EventBean bean);

        void onEventEdited();
    }
}
