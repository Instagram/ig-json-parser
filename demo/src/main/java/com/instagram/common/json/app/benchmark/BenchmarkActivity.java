/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.benchmark;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.instagram.common.json.app.igmodel.IgListOfModels;
import com.instagram.common.json.app.igmodel.IgModelRequest;
import com.instagram.common.json.app.igmodel.IgModelWorker;
import com.instagram.common.json.app.ommodel.OmListOfModels;
import com.instagram.common.json.app.ommodel.OmModelRequest;
import com.instagram.common.json.app.ommodel.OmModelWorker;
import com.instagram.common.json.app.utils.FileUtilsKt;
import com.instagram.jsonbenchmark.app.R;
import java.io.IOException;

public class BenchmarkActivity extends Activity {
  private String mJsonString;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.benchmark_activity);

    mJsonString = FileUtilsKt.rawResourceAsString(this, R.raw.benchmark_input);

    View.OnClickListener listener =
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            boolean useIgParser = (view == findViewById(R.id.ig_parse_button));
            boolean useOmParser = (view == findViewById(R.id.om_parse_button));

            BenchmarkStats bs = new BenchmarkStats();

            int iterations = getIterationCount();
            if (iterations == 1) {
              IgModelRequest igModel;
              OmModelRequest omModel;
              try {
                bs.before();
                if (useIgParser) {
                  igModel = new IgModelWorker().parseFromString(mJsonString);
                } else if (useOmParser) {
                  omModel = new OmModelWorker().parseFromString(mJsonString);
                }
                bs.after();
              } catch (IOException ex) {
                Toast.makeText(
                        BenchmarkActivity.this, "yeah, bad things happened", Toast.LENGTH_LONG)
                    .show();
                return;
              }
            } else {
              String multiIterationInputString = generateInputString(iterations);
              IgListOfModels igListofModels;
              OmListOfModels omListofModels;
              try {
                bs.before();
                if (useIgParser) {
                  igListofModels =
                      new IgModelWorker().parseListFromString(multiIterationInputString);
                } else if (useOmParser) {
                  omListofModels =
                      new OmModelWorker().parseListFromString(multiIterationInputString);
                }
                bs.after();
              } catch (IOException ex) {
                Toast.makeText(
                        BenchmarkActivity.this, "yeah, bad things happened", Toast.LENGTH_LONG)
                    .show();
                return;
              }
            }

            ((TextView) findViewById(R.id.results)).setText(bs.renderResultsToText());
          }
        };

    findViewById(R.id.ig_parse_button).setOnClickListener(listener);
    findViewById(R.id.om_parse_button).setOnClickListener(listener);

    findViewById(R.id.quit)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                System.exit(0);
              }
            });
  }

  private String generateInputString(int iterations) {
    StringBuilder sb = new StringBuilder();

    sb.append("{\"list\": [");

    for (int ix = 0; ix < iterations; ix++) {
      if (ix != 0) {
        sb.append(",");
      }
      sb.append(mJsonString);
    }

    sb.append("]}");

    return sb.toString();
  }

  int getIterationCount() {
    return Integer.valueOf(((EditText) findViewById(R.id.iterations)).getText().toString());
  }
}
