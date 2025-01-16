package za.co.permanentlink.payfast;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

/**
 * Abstract activity that facilitates PayFast payment integration.
 * Provides functionality to set up a payment process using Flutter,
 * validate input data, and handle payment events such as completion or cancellation.
 */
public abstract class PayFastActivity extends AppCompatActivity{
    /**
     * Payment data in JSON format.
     */
    JSONObject data;
    /**
     * The channel name used for communication between Flutter and native code.
     */
    private static final String CHANNEL = "com.xdev.payfast/payment";
    /**
     * List of allowed keys for payment data validation.
     */
    private List<String> _allowedKeys = List.of("merchant_id", "merchant_key", "name_first", "name_last", "email_address", "m_payment_id", "amount", "item_name");

    /**
     * Called when the activity is created.
     * Initializes the Flutter engine, sets up method channels for communication,
     * and starts the Flutter activity with the configured engine.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pay_fast);

        FlutterEngine flutterEngine=new FlutterEngine(this);
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );

        FlutterEngineCache
                .getInstance()
                .put("my_engine_id", flutterEngine);
        flutterEngine.getNavigationChannel().setInitialRoute("/");

        MethodChannel mc = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(),CHANNEL);
        mc.invokeMethod("initialise", data.toString());
        mc.setMethodCallHandler((methodCall, result) ->
            {
                if(methodCall.method.equals("onPaymentCompleted"))
                {
                    this.onPaymentCompleted();
                }

                if(methodCall.method.equals("onPaymentCancelled"))
                {
                    this.onPaymentCancelled();
                }
            }
        );

        startActivity(
                FlutterActivity
                        .withCachedEngine("my_engine_id")
                        .build(this)
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Sets the payment data and validates its contents.
     *
     * @param data The payment data in JSON format.
     * @throws Exception If the data contains invalid keys.
     */
    protected void setPaymentData(JSONObject data) throws Exception {
        this.data = data;
        this.validateInput(data.getJSONObject("data"));
    }

    /**
     * Validates the input JSON object to ensure all keys are allowed.
     *
     * @param object The JSON object containing payment data.
     * @throws Exception If any invalid key is found in the JSON object.
     */
    private void validateInput(JSONObject object) throws Exception {
        Iterator<?> keys = object.keys();
        while(keys.hasNext() ) {
            String key = (String) keys.next();
            if (!this._allowedKeys.contains(key)) {
                throw new Exception("Invalid key: " + key);
            }
        }
    }

    /**
     * Called when the payment process is cancelled.
     * This method must be implemented by subclasses to define specific behavior on payment cancellation.
     */
    protected abstract void onPaymentCancelled();
    /**
     * Called when the payment process is completed successfully.
     * This method must be implemented by subclasses to define specific behavior on payment completion.
     */
    protected abstract void onPaymentCompleted();

    /**
     * Sets the payment data and validates its contents.
     * The provided JSON object must only contain allowed keys.
     * Also, supports additional keys for configuration and customization.
     *
     * <p>Allowed Keys:</p>
     * <ul>
     *     <li><b>appBar.show</b> - Whether to show the app bar (boolean).</li>
     *     <li><b>appBar.title</b> - The title displayed in the app bar.</li>
     *     <li><b>appBar.backgroundColor</b> - The background color of the app bar (hex color code).</li>
     *     <li><b>payButtonText</b> - Text for the payment button.</li>
     *     <li><b>onPaymentCancelledText</b> - Text displayed when the payment is cancelled.</li>
     *     <li><b>onPaymentCompletedText</b> - Text displayed when the payment is completed.</li>
     *     <li><b>paymentSummaryTitle</b> - Title of the payment summary section.</li>
     *     <li><b>paymentSummaryAmountColor</b> - Color of the payment amount in the summary (hex color code).</li>
     * </ul>
     *
     * @param data A {@link JSONObject} containing the payment data.
     * @throws JSONException If inserts fail.
     */
    protected void setOptions(JSONObject data) throws JSONException {
        Iterator<?> keys = data.keys();
        while(keys.hasNext() ) {
            String key = (String) keys.next();
            this.data.put(key, data.get(key));
        }
    }
}