package live.vtb.nfc_test;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private final String[][] techListsArray = new String[][]{new String[]{android.nfc.tech.Ndef.class.getName()}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                processNfcMessages(msgs);
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle the URI from NFC
            handleUriIntent(intent);
        }
    }

    private void handleUriIntent(Intent intent) {
        String uri = intent.getDataString();
        if (uri != null) {
            Toast.makeText(this, "URI: " + uri, Toast.LENGTH_LONG).show();
            Log.d("NFC", "URI: " + uri);
            // Add logic to handle the URI and perform any specific action
        }
    }

    private void processNfcMessages(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        for (NdefMessage msg : msgs) {
            for (NdefRecord record : msg.getRecords()) {
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                        Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                    byte[] payload = record.getPayload();
                    String text = new String(payload, StandardCharsets.UTF_8);
                    Toast.makeText(this, "NFC Content: " + text, Toast.LENGTH_LONG).show();
                    Log.d("NFC", "NFC Content: " + text);
                    // Add logic to handle the content and perform any specific action
                }
            }
        }
    }
}
