package org.hqtp.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.inject.ContextSingleton;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;

@ContextSingleton
class TimelineAdapter extends BaseAdapter implements TimelineObserver {
    private final int FORM_CELL = 0;
    private final int POST_CELL = 1;
    private final int DATE_SEPARATOR_CELL = 2;

    @Inject
    LayoutInflater inflater;
    @Inject
    Activity activity;
    @Inject
    Alerter alerter;
    @Inject
    ImageLoader imageLoader;

    private List<ListCell> cells = new ArrayList<ListCell>();
    private PostingCell formCell;
    private DataSetObservable observable = new DataSetObservable();
    private int lectureId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private SimpleDateFormat dateSeparatorFormat = new SimpleDateFormat("yyyy/MM/dd");
    private final long ONE_HOUR_MILLIS = 60 * 60 * 1000;

    public TimelineAdapter() {
        cells.add(new DateSeparatorCell(new Date()));

        formCell = new PostingCell();
        cells.add(formCell);
    }

    public void setLectureId(int lectureId) {
        this.lectureId = lectureId;
    }

    @Override
    public int getCount() {
        return cells.size();
    }

    @Override
    public Object getItem(int position) {
        return cells.get(position).getItem();
    }

    @Override
    public long getItemId(int position) {
        return cells.get(position).getItemId();
    }

    @Override
    public int getItemViewType(int position) {
        return cells.get(position).getItemViewType();
    }

    @Override
    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        return cells.get(position).getView(convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return cells.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observable.unregisterObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return cells.get(position).isEnabled();
    }

    public class ItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            synchronized (cells) {
                final int currentPos = cells.indexOf(formCell);
                cells.remove(formCell);
                if (position < currentPos) {
                    cells.add(position + 1, formCell);
                } else {
                    cells.add(position, formCell);
                }
            }
            observable.notifyChanged();
            return true;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            synchronized (cells) {
                cells.remove(formCell);
                cells.add(formCell);
            }
            observable.notifyChanged();
            alerter.toastShort("投稿しました");
        }
    }

    @Override
    public void onUpdate(List<Post> posts) {
        synchronized (cells) {
            boolean formCellAdded = false;
            long beforeFormCellId = Long.MAX_VALUE;
            if (!cells.get(cells.size() - 1).equals(formCell)) {
                final int currentPos = cells.indexOf(formCell);
                beforeFormCellId = cells.get(currentPos - 1).getItemId();
            }

            cells.clear();
            Date prevDate = null;
            Date lastSeparatorDate = null;
            ListCell prevCell = null;
            for (Post post : posts) {
                Date currentDate = Post.virtualTimestampToDate(post.getVirtualTimestamp());
                if (prevDate == null || !isSameDate(prevDate, currentDate)) {
                    lastSeparatorDate = currentDate;
                    prevCell = new DateSeparatorCell(currentDate);
                    cells.add(prevCell);
                    if (!formCellAdded && prevCell.getItemId() == beforeFormCellId) {
                        cells.add(formCell);
                        formCellAdded = true;
                    }
                }
                prevDate = currentDate;

                prevCell = new PostCell(post);
                cells.add(prevCell);
                if (!formCellAdded && prevCell.getItemId() == beforeFormCellId) {
                    cells.add(formCell);
                    formCellAdded = true;
                }
            }

            if (lastSeparatorDate == null || !isSameDate(lastSeparatorDate, new Date())) {
                cells.add(new DateSeparatorCell(new Date()));
            }

            if (!formCellAdded) {
                cells.add(formCell);
            }
            observable.notifyChanged();
        }
    }

    private boolean isSameDate(Date date1, Date date2) {
        return (date1.getYear() == date2.getYear() &&
                date1.getMonth() == date2.getMonth() && date1.getDay() == date2.getDay());
    }

    private abstract class ListCell {
        public abstract View getView(View convertView, ViewGroup parent);

        public abstract Object getItem();

        public abstract long getItemId();

        public abstract int getItemViewType();

        public abstract boolean isEnabled();

        public abstract long getVirtualTimestamp();
    }

    private class PostingCell extends ListCell {
        private View postView = null;

        @Override
        public View getView(View convertView, ViewGroup parent) {
            if (postView == null) {
                postView = inflater.inflate(R.layout.timeline_posting_cell, null);
                Button postButton = (Button) postView.findViewById(R.id.postButton);
                postButton.setOnClickListener(new PostButtonOnClickListener());
            }
            return postView;
        }

        @Override
        public Object getItem() {
            return null;
        }

        @Override
        public long getItemId() {
            return Long.MAX_VALUE;
        }

        @Override
        public int getItemViewType() {
            return FORM_CELL;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public long getVirtualTimestamp() {
            throw new IllegalArgumentException("You cannot call getVirtualTimestamp of FormCell.");
        }

        private class PostButtonOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                final int currentPos = cells.indexOf(formCell);
                long prevVirtualTimestamp = -1;
                long nextVirtualTimestamp = -1;
                if (currentPos != cells.size() - 1) {
                    // The form position is not the end of the list.
                    if (currentPos == 0) {
                        prevVirtualTimestamp = 0;
                        nextVirtualTimestamp = cells.get(currentPos + 1).getVirtualTimestamp();
                    } else {
                        prevVirtualTimestamp = cells.get(currentPos - 1).getVirtualTimestamp();
                        nextVirtualTimestamp = cells.get(currentPos + 1).getVirtualTimestamp();
                    }
                }

                Intent intent = new Intent(activity, PostTimelineActivity.class);
                intent.putExtra(PostTimelineActivity.LECTURE_ID, lectureId);
                intent.putExtra(PostTimelineActivity.PREV_VIRTUAL_TS, prevVirtualTimestamp);
                intent.putExtra(PostTimelineActivity.NEXT_VIRTUAL_TS, nextVirtualTimestamp);

                activity.startActivityForResult(intent, 0);
            }
        }

    }

    private class PostCell extends ListCell {
        private final Post post;

        public PostCell(Post post) {
            this.post = post;
        }

        @Override
        public View getView(View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.timeline_post_cell, null);
            }
            TextView bodyView = (TextView) convertView.findViewById(R.id.postContent);
            TextView userNameView = (TextView) convertView.findViewById(R.id.userName);
            TextView postedTimeView = (TextView) convertView.findViewById(R.id.postedTime);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.userIcon);

            bodyView.setText(post.getBody());
            userNameView.setText(post.getUser().getName());
            imageView.setTag(post.getUser().getIconURL());
            imageLoader.displayImage(imageView, TimelineAdapter.this.activity);
            final Date postedDate = post.getTime();
            final long diffMillis = new Date().getTime() - postedDate.getTime();
            if (diffMillis < ONE_HOUR_MILLIS) {
                postedTimeView.setText((diffMillis / 1000 / 60) + "分前");
            } else {
                postedTimeView.setText(dateFormat.format(postedDate));
            }

            return convertView;
        }

        @Override
        public Object getItem() {
            return post;
        }

        @Override
        public long getItemId() {
            return post.getId();
        }

        @Override
        public int getItemViewType() {
            return POST_CELL;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public long getVirtualTimestamp() {
            return post.getVirtualTimestamp();
        }
    }

    private class DateSeparatorCell extends ListCell {
        private final Date date;

        public DateSeparatorCell(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            this.date = cal.getTime();
        }

        @Override
        public View getView(View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.timeline_date_separator_cell, null);
            }
            TextView dateText = (TextView) convertView.findViewById(R.id.dateText);
            dateText.setText(dateSeparatorFormat.format(date));

            return convertView;
        }

        @Override
        public Object getItem() {
            return null;
        }

        @Override
        public long getItemId() {
            return -date.getTime();
        }

        @Override
        public int getItemViewType() {
            return DATE_SEPARATOR_CELL;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public long getVirtualTimestamp() {
            return Post.dateToVirtualTimestamp(date);
        }
    }
}
