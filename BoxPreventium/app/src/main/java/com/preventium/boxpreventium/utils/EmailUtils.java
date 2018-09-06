package com.preventium.boxpreventium.utils;

import android.content.Context;

import com.preventium.boxpreventium.module.ErrorEmail.GMailSender;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by tog on 30/08/2018.
 */

public class EmailUtils extends ByteArrayOutputStream {

    private final int MAX_STACK_TRACE_SIZE = 131071;

    private Context context;
    private Exception exception;
    private String content;

    public EmailUtils (Exception exception) {
        this.exception = exception;
    }

    public EmailUtils (String content) {
        this.content = content;
    }

    public void send (String subject) {
        PrintStream ps;
        if( this.exception != null ) {
            ps = new PrintStream(this);
            this.exception.printStackTrace(ps);
        }

        /* send */
        new GMailSender()
                .addSubject(subject)
                .addBody(this.toString())
                .send();

    }

    public String toString () {
        if( this.exception != null )
            return super.toString();
        return this.content.toString();
    }

}
