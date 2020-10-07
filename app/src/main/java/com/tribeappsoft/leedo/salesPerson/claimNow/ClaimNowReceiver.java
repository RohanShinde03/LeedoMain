package com.tribeappsoft.leedo.salesPerson.claimNow;

public class ClaimNowReceiver //extends BroadcastReceiver
{

//    private String TAG = "ClaimNowReceiver";
//
//    @Override
//    public void onReceive(Context context, Intent intent)
//    {
//        Log.e(TAG, "onReceive: Claim Now ");
//
//        if (intent!=null){
//            String page  = intent.getStringExtra("page");
//            boolean notifyFeeds  = intent.getBooleanExtra("notifyFeeds", false);
//            Log.e(TAG, "intent page "+page);
//            callToClaimNow(context, page, notifyFeeds);
//        }
//    }
//
//    private void callToClaimNow(Context context, String page, boolean notifyFeeds)
//    {
//        context.startActivity(new Intent(context, ClaimNowActivity.class)
//                .putExtra("page", page)
//                .putExtra("notifyFeeds", notifyFeeds)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        );
//    }
}
