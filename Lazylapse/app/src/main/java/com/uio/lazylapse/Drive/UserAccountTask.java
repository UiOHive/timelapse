package com.uio.lazylapse.Drive;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.uio.lazylapse.Interface.Controller;

/**
 * used to retrieve the dropbox account of the authenticated user to get information such as email,
 * username and such. Maybe more cosmetic than useful, still helps when debugging and to know if
 * authentication has worked. Used in {@link Controller#getUserAccount()}
 *
 * authored by Valdio Veliu on https://www.sitepoint.com/adding-the-dropbox-api-to-an-android-app/
 */
public class UserAccountTask extends AsyncTask<Void, Void, FullAccount> {

        private DbxClientV2 dbxClient;
        private TaskDelegate  delegate;
        private Exception error;

        public interface TaskDelegate {
            void onAccountReceived(FullAccount account);
            void onError(Exception error);
        }

        public UserAccountTask(DbxClientV2 dbxClient, TaskDelegate delegate){
            this.dbxClient =dbxClient;
            this.delegate = delegate;
        }

        @Override
        protected FullAccount doInBackground(Void... params) {
            try {
                //get the users FullAccount
                return dbxClient.users().getCurrentAccount();
            } catch (DbxException e) {
                e.printStackTrace();
                error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(FullAccount account) {
            super.onPostExecute(account);

            if (account != null && error == null){
                //User Account received successfully
                delegate.onAccountReceived(account);
            }
            else {
                // Something went wrong
                delegate.onError(error);
            }
        }
    }

