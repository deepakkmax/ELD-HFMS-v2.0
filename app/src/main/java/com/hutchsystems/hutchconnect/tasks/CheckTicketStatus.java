package com.hutchsystems.hutchconnect.tasks;

import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.beans.TicketBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.WebService;
import com.hutchsystems.hutchconnect.common.WebUrl;
import com.hutchsystems.hutchconnect.db.HelpDB;

import org.json.JSONObject;

import java.util.ArrayList;
// Rename the class name because same class name found in the Help Fragment 
public class CheckTicketStatus extends AsyncTask<Void, Void, TicketBean> {
    private CheckTicketStatus.PostTaskListener<TicketBean> postTaskListener;


        public CheckTicketStatus(CheckTicketStatus.PostTaskListener<TicketBean> postTaskListener) {
            this.postTaskListener = postTaskListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected TicketBean doInBackground(Void... voids) {
            TicketBean ticket = PostTicketStatus();
            return ticket;
        }

        @Override
        protected void onPostExecute(TicketBean ticket) {
            super.onPostExecute(ticket);
            this.postTaskListener.onPostTask(ticket);

        }




    // Created By: Pallavi Wattamwar
    // Created Date: 27 September 2018
    // Purpose: check Status of the ticket
    public TicketBean PostTicketStatus() {

        WebService ws = new WebService();

        ArrayList<TicketBean> ticketList = HelpDB.Get();
        TicketBean ticket = new TicketBean();
        if(ticketList.size() > 0)
        {
            for(int i=0;  i < ticketList.size(); i++) {

                try {
                    ticket = ticketList.get(i);
                    String ticketDate = Utility.getCurrentPSTDateTime();
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", ticket.getTicketId());
                    obj.put("TicketTypeId", 4);
                    obj.put("TicketDate", ticketDate);
                    obj.put("Title", ticket.getTitle());
                    obj.put("Comment", ticket.getComment());
                    obj.put("VehicleId", Utility.vehicleId);
                    obj.put("CompanyId", Utility.companyId);
                    obj.put("CreatedBy", Utility.onScreenUserId);
                    obj.put("CreatedDate", ticketDate);
                    obj.put("ModifiedBy", Utility.onScreenUserId);
                    obj.put("ModifiedDate", ticketDate);
                    obj.put("StatusId", ticket.getTicketStatus());

                    String result = ws.doPost(
                            WebUrl.TICKET_POST,
                            obj.toString());

                    if (result != null) {
                        JSONObject response = new JSONObject(result);

                        int id = response.getInt("TicketId");
                        String ticketNo = response.getString("TicketNo");
                        int clientStatusId = response.getInt("ClientStatusId");
                        if (id > 0) {
                            ticket.setTicketId(id);
                            ticket.setTicketNo(ticketNo);
                            ticket.setTicketStatus(clientStatusId);
                            HelpDB.Save(ticket.getType(), id, ticketNo, ticket.getTitle(), ticket.getComment(), ticketDate, clientStatusId);
                        }
                    }

                } catch (Exception e) {
                    ticket.setTicketNo(e.getMessage());
                    e.printStackTrace();
                    Utility.printError(e.getMessage());
                }
            }
        }

        return ticket;
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }




    }