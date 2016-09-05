package com.qiscus.library.chat.data.local;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.data.model.QiscusComment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public enum DataBaseHelper {
    INSTANCE;

    private final SQLiteDatabase sqLiteDatabase;

    DataBaseHelper() {
        DbOpenHelper dbOpenHelper = new DbOpenHelper(Qiscus.getApps());
        sqLiteDatabase = dbOpenHelper.getReadableDatabase();
    }

    public static DataBaseHelper getInstance() {
        return INSTANCE;
    }

    public void add(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(Db.CommentTable.TABLE_NAME, null, Db.CommentTable.toContentValues(qiscusComment));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    public void saveLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainFileOfComment(commentId)) {
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.insert(Db.FilesTable.TABLE_NAME, null,
                                      Db.FilesTable.toContentValues(topicId, commentId, localPath));
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
    }

    public boolean isContains(QiscusComment qiscusComment) {
        String query;
        if (qiscusComment.getId() == -1) {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public boolean isContainFileOfComment(int commentId) {
        String query = "SELECT * FROM "
                + Db.FilesTable.TABLE_NAME + " WHERE "
                + Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        boolean contains = cursor.getCount() > 0;
        cursor.close();
        return contains;
    }

    public void update(QiscusComment qiscusComment) {
        String where;
        if (qiscusComment.getId() == -1) {
            where = Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            where = Db.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(Db.CommentTable.TABLE_NAME, Db.CommentTable.toContentValues(qiscusComment), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void updateLocalPath(int topicId, int commentId, String localPath) {
        String where = Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.update(Db.FilesTable.TABLE_NAME, Db.FilesTable.toContentValues(topicId, commentId, localPath), where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void addOrUpdate(QiscusComment qiscusComment) {
        if (!isContains(qiscusComment)) {
            add(qiscusComment);
        } else {
            update(qiscusComment);
        }
    }

    public void addOrUpdateLocalPath(int topicId, int commentId, String localPath) {
        if (!isContainFileOfComment(commentId)) {
            saveLocalPath(topicId, commentId, localPath);
        } else {
            updateLocalPath(topicId, commentId, localPath);
        }
    }

    public void delete(QiscusComment qiscusComment) {
        String where;
        if (qiscusComment.getId() == -1) {
            where = Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        } else {
            where = Db.CommentTable.COLUMN_ID + " = " + qiscusComment.getId() + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + qiscusComment.getUniqueId() + "'";
        }

        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(Db.CommentTable.TABLE_NAME, where, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public File getLocalPath(int commentId) {
        String query = "SELECT * FROM "
                + Db.FilesTable.TABLE_NAME + " WHERE "
                + Db.FilesTable.COLUMN_COMMENT_ID + " = " + commentId + "";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            File file = new File(Db.FilesTable.parseCursor(cursor));
            cursor.close();
            if (file.exists()) {
                return file;
            }
            return null;
        } else {
            cursor.close();
            return null;
        }
    }

    public QiscusComment getComment(int id, String uniqueId) {
        String query;
        if (id == -1) {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + id + "'";
        } else {
            query = "SELECT * FROM "
                    + Db.CommentTable.TABLE_NAME + " WHERE "
                    + Db.CommentTable.COLUMN_ID + " = " + id + " OR "
                    + Db.CommentTable.COLUMN_UNIQUE_ID + " = '" + uniqueId + "'";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToNext()) {
            QiscusComment qiscusComment = Db.CommentTable.parseCursor(cursor);
            cursor.close();
            return qiscusComment;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<QiscusComment> getComments(int topicId, int count) {
        String query = "SELECT * FROM "
                + Db.CommentTable.TABLE_NAME + " WHERE "
                + Db.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " "
                + "ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(Db.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    public Observable<List<QiscusComment>> getObservableComments(final int topicId, final int count) {
        return Observable.create((Observable.OnSubscribe<List<QiscusComment>>) subscriber -> {
            subscriber.onNext(getComments(topicId, count));
            subscriber.onCompleted();
        });
    }

    public List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, int topicId, int count) {
        String query = "SELECT * FROM "
                + Db.CommentTable.TABLE_NAME + " WHERE "
                + Db.CommentTable.COLUMN_TOPIC_ID + " = " + topicId + " AND "
                + Db.CommentTable.COLUMN_TIME + " < " + qiscusComment.getTime().getTime() + " "
                + "ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC "
                + "LIMIT " + count;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<QiscusComment> qiscusComments = new ArrayList<>();
        while (cursor.moveToNext()) {
            qiscusComments.add(Db.CommentTable.parseCursor(cursor));
        }
        cursor.close();
        return qiscusComments;
    }

    public Observable<List<QiscusComment>> getObservableOlderCommentsThan(final QiscusComment qiscusComment, final int topicId, final int count) {
        return Observable.create((Observable.OnSubscribe<List<QiscusComment>>) subscriber -> {
            subscriber.onNext(getOlderCommentsThan(qiscusComment, topicId, count));
            subscriber.onCompleted();
        });
    }

    public void clear() {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(Db.FilesTable.TABLE_NAME, null, null);
            sqLiteDatabase.delete(Db.CommentTable.TABLE_NAME, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}
