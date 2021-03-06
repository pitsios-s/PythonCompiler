/* This file was generated by SableCC (http://www.sablecc.org/). */

package minipython.node;

import minipython.analysis.*;

public final class TGrt extends Token
{
    public TGrt()
    {
        super.setText(">");
    }

    public TGrt(int line, int pos)
    {
        super.setText(">");
        setLine(line);
        setPos(pos);
    }

    public Object clone()
    {
      return new TGrt(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTGrt(this);
    }

    public void setText(String text)
    {
        throw new RuntimeException("Cannot change TGrt text.");
    }
}
