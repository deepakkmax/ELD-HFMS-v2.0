package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.MessageAdapter;
import com.hutchsystems.hutchconnect.beans.MessageBean;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MessageDB;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment implements ChatClient.ChatMessageInterface {

    ListView lvMessage;
    EditText etMessage;
    ImageButton btnSend;
    public static int userId = 0;
    String userName = "";
    ArrayList<MessageBean> messageList;
    MessageAdapter adapter;
    OnFragmentInteractionListener mListener;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(int id, String name) {
        MessageFragment fragment = new MessageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", id);
        bundle.putString("userName", name);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        userId = 0;
    }


    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt("userId");
            userName = getArguments().getString("userName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imgBackground.setBackgroundResource(0);
        imgBackground.setBackgroundResource(R.drawable.message_screen_bg);
    }

    ImageView imgBackground;

    private void initialize(View view) {
        ChatClient.mListener = this;

        imgBackground = (ImageView) view.findViewById(R.id.imgBackground);
        lvMessage = (ListView) view.findViewById(R.id.lvMessage);
        etMessage = (EditText) view.findViewById(R.id.etMessage);
        btnSend = (ImageButton) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isInternetOn()) {

                    if (etMessage.length() > 0) {
                        MessageBean message = MessageDB.CreateMessage(etMessage.getText().toString(), Utility.onScreenUserId, userId, "Message");

                        MessageDB.Send(message);

                        etMessage.setText("");
                        message.setMessageDate(Utility.convertUTCToLocalDateTime(message.getMessageDate()));
                        messageList.add(message);
                        adapter.notifyDataSetChanged();
                        lvMessage.smoothScrollToPosition(lvMessage.getCount() - 1);

                    }
                } else {
                    Utility.showMsg(getString(R.string.internet_connection_alert));
                }
            }
        });

        // set read status
        if (MessageDB.getUnreadStatus(userId)) {
            MessageStatusSend();
            MessageDB.MessageStatusUpdate(userId, Utility.onScreenUserId);
        }
        getMessage();
    }

    private void MessageStatusSend() {

        MessageBean message = MessageDB.CreateMessage("", userId, Utility.onScreenUserId, "Read");
        MessageDB.Send(message);
    }

    private void getMessage() {
        messageList = MessageDB.getMessage(userId);
        adapter = new MessageAdapter(R.layout.fragment_message, messageList);
        lvMessage.setAdapter(adapter);
        // lvMessage.smoothScrollToPosition(lvMessage.getCount() - 1);
    }

    public void received(MessageBean bean) {
        try {
            if (lvMessage == null || bean.getCreatedById() != userId)
                return;
            String localDate = Utility.convertUTCToLocalDateTime(bean.getMessageDate());
            bean.setMessageDate(localDate);

            messageList.add(bean);
            adapter.notifyDataSetChanged();
            lvMessage.smoothScrollToPosition(lvMessage.getCount() - 1);

            MessageDB.MessageStatusUpdate(userId, Utility.onScreenUserId);
            // send message status
            MessageStatusSend();
        } catch (Exception e) {
            e.printStackTrace();
            LogFile.write(MainActivity.class.getName() + "::received Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(MessageFragment.class.getName(),"received Error ",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void MessageStatusUpdate(MessageBean bean) {
        try {
            if (lvMessage == null || bean.getCreatedById() != Utility.onScreenUserId)
                return;
            for (MessageBean message : messageList) {
                message.setReadFg(1);
            }

            adapter.notifyDataSetChanged();
            lvMessage.smoothScrollToPosition(lvMessage.getCount() - 1);

        } catch (Exception e) {
            e.printStackTrace();
            LogFile.write(MainActivity.class.getName() + "::received Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(MessageFragment.class.getName(),"MessageStatusUpdate",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onMessageUpdated(final MessageBean obj, final int flag) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag == ChatClient.MESSAGE)
                    received(obj);
                else if (flag == ChatClient.READFG)
                    MessageStatusUpdate(obj);

            }
        });
    }
}