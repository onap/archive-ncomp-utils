
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.ncomp.utils.commands;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Commands {
	private class Command {
		private final Exec cmd;
		private final String label;
		private final String description;

		public Command (Exec cmd, String label, String description) {
			this.cmd = cmd;
			this.label = label;
			this.description = description;
		}
		public String description () {
			return description;
		}
		public String label () {
			return label;
		}
		public void execute (List<String> argv) throws Exception {
			String errmsg = cmd.execute(argv);
			if (errmsg != null) error (errmsg);
		}
		public void error (String msg) {
			err.print(label());
			err.print(": ");
			err.println(msg);
			err.print("Syntax: ");
			err.println(description());
		}
		public boolean matches (String s) {
			return s.startsWith(label());
		}
		public boolean argc_ok (int argc) {
			return ((argc >= cmd.min_args()) && (cmd.max_args() == -1 || argc <= cmd.max_args()));
		}
	}

	List<Command> cmdlist;
	PrintStream err;
	PrintStream help;
	
	public ExecImpl cmdHelp;
	public ExecImpl cmdNest;
	public Commands () {
		cmdlist = new ArrayList<Command>();
		err = System.err;
		help = System.out;
		cmdHelp = new ExecImpl(0) {
			public String execute (List<String> argv) {
				Commands.this.help();
				return null;
			}
		};
		cmdNest = new ExecImpl(1,-1) {
			public String execute(List<String> argv) throws Exception {
				Commands.this.execute (argv);
				return null;
			}				
		};
	}
	public void add (Command cmd) {
		cmdlist.add(cmd);
	}
	public void add (Exec cmd, String label, String description) {
		cmdlist.add(new Command(cmd, label, description));
	}
	public void execute (List<String> argv) throws Exception {
		if (argv.isEmpty()) return;
		String cmd = argv.get(0);
		argv = argv.subList(1,argv.size());
		for (Command c : cmdlist) {
			if (c.matches(cmd)) {
				if (!c.argc_ok (argv.size())) {
					c.error("Wrong number of arguments");
					return;
				}
				c.execute (argv);
				return;
			}
		}
		if (err != null) err.println ("Invalid command: " + cmd);
		help();
	}
	public void help () {
		if (help == null) return;
		help.println("Commands:");
		for (Command c : cmdlist) {
			if (c.description().startsWith(c.label())) {
				help.print(c.label());
				help.print("|");
				help.println(c.description().substring(c.label().length()));
			} else {
				help.print(c.label());
				help.print(" ");
				help.println(c.description());
			}
		}
	}
	public void set_err_stream (PrintStream p) {
		err = p;
	}
	public PrintStream get_err_stream () {
		return err;
	}
	public void set_help_stream (PrintStream p) {
		help = p;
	}
	public PrintStream get_help_stream () {
		return help;
	}
	public void set_streams (PrintStream err, PrintStream help) {
		this.err = err;
		this.help = help;
	}
}
