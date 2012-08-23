/*
 * Copyright (C) 2012  Armin Häberling
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package ch.corten.aha.worldclock;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WeatherWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WeatherWidgetViewsFactory(this.getApplicationContext());
    }

    static class WeatherWidgetViewsFactory implements RemoteViewsFactory {
        private Context mContext;
        private Cursor mCursor;

        public WeatherWidgetViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
        }

        private static final String[] PROJECTION = {
            Clocks._ID,
            Clocks.TIMEZONE_ID,
            Clocks.CITY,
            Clocks.TEMPERATURE,
            Clocks.CONDITION_CODE,
            Clocks.WEATHER_CONDITION,
            Clocks.LATITUDE,
            Clocks.LONGITUDE
        };

        @Override
        public void onDataSetChanged() {
            // Refresh the cursor
            if (mCursor != null) {
                mCursor.close();
            }

            mCursor = mContext.getContentResolver().query(Clocks.CONTENT_URI,
                    PROJECTION, Clocks.USE_IN_WIDGET + " = 1", null,
                    Clocks.TIME_DIFF + " ASC, " + Clocks.CITY + " ASC");
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv;
            if (position == 0) {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.weather_widget_item2);
            } else {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.weather_widget_item);
            }

            if (mCursor.moveToPosition(position)) {
                rv.setTextViewText(R.id.city_text, mCursor.getString(mCursor.getColumnIndex(Clocks.CITY)));
                
                String id = mCursor.getString(mCursor.getColumnIndex(Clocks.TIMEZONE_ID));
                DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
                Date date = new Date();
                TimeZone tz = TimeZone.getTimeZone(id);
                rv.setTextViewText(R.id.time_text, TimeZoneInfo.showTime(tz, date, df, true));

                rv.setTextViewText(R.id.condition_text, mCursor
                        .getString(mCursor.getColumnIndex(Clocks.WEATHER_CONDITION)));
                
                String temperature = BindHelper.getTemperature(mContext, mCursor, false);
                rv.setTextViewText(R.id.temp_text, temperature);

                int condCode = mCursor.getInt(mCursor.getColumnIndex(Clocks.CONDITION_CODE));
                double lat = mCursor.getDouble(mCursor.getColumnIndex(Clocks.LATITUDE));
                double lon = mCursor.getDouble(mCursor.getColumnIndex(Clocks.LONGITUDE));
                rv.setImageViewResource(R.id.condition_image, WeatherIcons.getIcon(condCode, lon, lat));

                Intent intent = new Intent();
                rv.setOnClickFillInIntent(R.id.widget_item, intent);
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            // we don't use a special loading view.
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mCursor.getColumnIndex(Clocks._ID));
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
