//____________________________________________________________________________
// Expr ::= Var
//        |  Int
//        |  Expr + Expr
//        |  Expr - Expr
abstract class Expr {
  abstract Value    eval(Env env);
  abstract String show();

  Env evalRef(Env env) {
    return new ValEnv("", eval(env), null);
  }
}

class Var extends Expr {
  private String name;
  Var(String name) { this.name = name; }

  Value   eval(Env env) { return Env.lookup(env, name).getValue(); }
  Env evalRef(Env env) { return Env.lookup(env, name); }
  String show() { return name; }
}

class Int extends Expr {
  private int num;
  Int(int num) { this.num = num; }

  Value   eval(Env env) { return new IValue(num); }
  String show() { return Integer.toString(num); }
}

class Plus extends Expr {
  private Expr l, r;
  Plus(Expr l, Expr r) { this.l = l; this.r = r; }

  Value    eval(Env env) { return new IValue( l.eval(env).asInt()   + r.eval(env).asInt() ); }
  String show() { return "(" + l.show() + " + " + r.show() + ")"; }
}

class Mult extends Expr {
  private Expr l, r;
  Mult(Expr l, Expr r) { this.l = l; this.r = r; }

  Value    eval(Env env) { return new IValue( l.eval(env).asInt() * r.eval(env).asInt() ); }
  String show() { return "(" + l.show() + " * " + r.show() + ")"; }
}

class Minus extends Expr {
  private Expr l, r;
  Minus(Expr l, Expr r) { this.l = l; this.r = r; }

  Value    eval(Env env) { return new IValue( l.eval(env).asInt() - r.eval(env).asInt() ); }
  String show() { return "(" + l.show() + " - " + r.show() + ")"; }
}

//____________________________________________________________________________
// Expr ::= Expr < Expr
//        |  Expr == Expr

class LT extends Expr {
  private Expr l, r;
  LT(Expr l, Expr r) { this.l = l; this.r = r; }

  Value eval(Env env) { return new BValue( l.eval(env).asInt() < r.eval(env).asInt() ); }
  String show()  { return "(" + l.show() + " < " + r.show() + ")"; }
}

class EqEq extends Expr {
  private Expr l, r;
  EqEq(Expr l, Expr r) { this.l = l; this.r = r; }

  Value eval(Env env) { return new BValue( l.eval(env).asInt() == r.eval(env).asInt() ); }
  String show()  { return "(" + l.show() + " == " + r.show() + ")"; }
}

//____________________________________________________________________________
// Stmt  ::= Seq Stmt Stmt
//        |  Var := Expr
//        |  While Expr Stmt
//        |  If Expr Stmt Stmt
//        |  Print Expr

abstract class Stmt {
  abstract Env exec(Program prog, Env env);
  abstract void print(int ind);

  static void indent(int ind) {
    for (int i=0; i<ind; i++) {
      System.out.print(" ");
    }
  }
}

class Seq extends Stmt {
  private Stmt l, r;
  Seq(Stmt l, Stmt r) { this.l = l; this.r = r; }

  Env exec(Program prog, Env env) {
    return r.exec(prog, l.exec(prog,env));
  }

  void print(int ind) {
    l.print(ind);
    r.print(ind);
  }
}

class Assign extends Stmt {
  private String lhs;
  private Expr  rhs;
  Assign(String lhs, Expr rhs) {
    this.lhs = lhs; this.rhs = rhs;
  }

  Env exec(Program prog, Env env) {
     Env.lookup(env, lhs).setValue(rhs.eval(env)); 
     return env;
  }

  void print(int ind) {
    indent(ind);
    System.out.println("var " + lhs + " = " + rhs.show() + ";");
  }
}

class VarDecl extends Stmt {
    private String id;
    private Expr rhs;
    VarDecl(String id, Expr rhs) {
        this.id = id; this.rhs = rhs;
    }

    Env exec(Program prog, Env env) {
        return new ValEnv(id, rhs.eval(env), env);
    }
    
    void print(int ind) {
        indent(ind);
        System.out.println(id + " = " + rhs.show() + ";");
    }

}

class While extends Stmt {
  private Expr test;
  private Stmt  body;
  While(Expr test, Stmt body) {
    this.test = test; this.body = body;
  }

  Env exec(Program prog, Env env) {
    while (test.eval(env).asBool()) {
      body.exec(prog, env);
    }
    return env;
  }

  void print(int ind) {
    indent(ind);
    System.out.println("while (" + test.show() + ") {");
    body.print(ind+2);
    indent(ind);
    System.out.println("}");
  }
}

class If extends Stmt {
  private Expr test;
  private Stmt  t, f;
  If(Expr test, Stmt t, Stmt f) {
    this.test = test; this.t = t; this.f = f;
  }

  Env exec(Program prog, Env env) {
    if (test.eval(env).asBool()) {
        t.exec(prog, env);
    } else {
        f.exec(prog, env);
    }
    return env;
  }

  void print(int ind) {
    indent(ind);
    System.out.println("if (" + test.show() + ") {");
    t.print(ind+2);
    indent(ind);
    System.out.println("} else {");
    f.print(ind+2);
    indent(ind);
    System.out.println("}");
  }
}

class Print extends Stmt {
  private Expr exp;
  Print(Expr exp) { this.exp = exp; }

  Env exec(Program prog, Env env) {
    System.out.println("Output: " + exp.eval(env).asInt());
    return env;
  }

  void print(int ind) {
    indent(ind);
    System.out.println("print " + exp.show() + ";");
  }
}

class Call extends Stmt {
    private String name;
    private Expr[] actuals;
    Call(String name, Expr[] actuals) {
        this.name = name; this.actuals = actuals;
    }

    Env exec(Program prog, Env env) {
        prog.call(env, name, actuals);
        return env;
    }

    void print(int ind) {
        int len = actuals.length;
        indent(ind);
        System.out.print("void " + name + "( ");
        for(int i = 0; i < len; ++i) {
            System.out.print( actuals[i] + " ");
        }
        System.out.print(")");
    }
}

class Formal{
    protected String name;

    Formal(String name) { this.name = name; }

    public String toString() { return this.name; } 

    Env extend(Env env, Expr expr, Env rest) {
        return new ValEnv(name, expr.eval(env), rest);
    }
}

class ByRef extends Formal {
    ByRef(String name) { super(name); }
    
    public String toString() { return "ref" + name; }

    Env extend(Env env, Expr expr, Env rest) {
        return new RefEnv(name, expr.evalRef(env), rest);
    }
}

class Proc {
    private String name;
    private Formal[] formals;
    private Stmt body;   

    Proc(String name, Formal[] formals, Stmt body) { this.name = name; this.formals = formals; this.body = body; }   

    void call(Program prog, Env env, Expr[] actuals) {
      if (actuals.length!=formals.length) {
        System.out.println("ABORT: Wrong number of arguments for " + name);
        System.exit(1);
      }
      Env newenv = null;
      for (int i=0; i<actuals.length; i++) {
        newenv = formals[i].extend(env, actuals[i], newenv);
      }
      body.exec(prog, newenv);
    }

    String getName() { return this.name; }
}



abstract class Value {
    abstract String show();
    
    boolean asBool() {
        System.out.println("ABORT: Boolean value expected");
        System.exit(1);
        return true; // Not reached
    }

    int asInt() {
        System.out.println("ABORT: Int value expected");
        System.exit(1);
        return 1; // Not reached

    }
}

class BValue extends Value {
    private boolean b;
        BValue(boolean b) { this.b = b; }

        String show() {
            return Boolean.toString(b);
        }

        boolean asBool() { return b; }
}

class IValue extends Value {
    private int i;
        IValue(int i) { this.i = i; }

        String show() {
            return Integer.toString(i);
        }

        int asInt() { return i; }
}
