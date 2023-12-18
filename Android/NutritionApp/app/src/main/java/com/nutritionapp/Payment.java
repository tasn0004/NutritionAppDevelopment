package com.nutritionapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class Payment extends AppCompatActivity {

    private BillingClient billingClient;
    private SkuDetails subscriptionSkuDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        initializeBillingClient();
        setupSubscriptionButton();
    }

    private void initializeBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                        // Handle the purchase update (success, failure, etc.)
                    }
                })
                .enablePendingPurchases()
                .build();

        connectBillingClient();
    }

    private void connectBillingClient() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    querySubscriptionDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play.
            }
        });
    }

    private void querySubscriptionDetails() {
        List<String> skuList = new ArrayList<>();
        skuList.add("kentaobida");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);

        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Retrieve the SkuDetails object and save it for the purchase flow.
                        if (!skuDetailsList.isEmpty()) {
                            subscriptionSkuDetails = skuDetailsList.get(0);
                        }
                    }
                });
    }

    private void setupSubscriptionButton() {
        Button subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (subscriptionSkuDetails != null) {
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(subscriptionSkuDetails)
                            .build();
                    billingClient.launchBillingFlow(Payment.this, flowParams);
                }
            }
        });
    }
}
