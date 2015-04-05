class Program {
    private Proc[] procs;
    private Stmt body;

    Program(Proc[] procs, Stmt body) {
      this.procs = procs; this.body = body;
    }

    Program(Stmt body) {
      this(new Proc[] {}, body);
    }
    void run() {
        body.exec(this, null);
    }

    void print() {
        body.print(4);
        System.out.println();
    }

    void call(Env env, String name, Expr[] actuals) {
        for (int i=0; i<procs.length; i++) {
          if (name.equals(procs[i].getName())) {
            procs[i].call(this, env, actuals);
            return;
          }
        }
        System.out.println("ABORT: Cannot find function " + name);
        System.exit(1);
    }
}
