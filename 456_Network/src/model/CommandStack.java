package model;

import java.util.Stack;

public class CommandStack {

	private Stack<ICommand> done;
	
	private Stack<ICommand> undone;
	
	public CommandStack() {
		this.done = new Stack<ICommand>();
		this.undone = new Stack<ICommand>();
	}
	
	public final void Do(final ICommand ... commands) {
		ICommand cmd = new ICommand() {
			public void execute() {
				for (ICommand command : commands) {
					command.execute();
				}
			}

			public void unexecute() {
				for (int i = commands.length-1; i >= 0; i--) {
					commands[i].unexecute();
				}
			}
		};
		this.undone.clear();
		done.push(cmd);
		cmd.execute();
	}
	
	public boolean canRedo() {
		return !this.undone.isEmpty();
	}
	
	public void redo() {
		this.done.push(this.undone.pop()).execute();
	}
	
	public boolean canUndo() {
		return !this.done.isEmpty();
	}
	
	public void undo() {
		this.undone.push(this.done.pop()).unexecute();
	}
}
