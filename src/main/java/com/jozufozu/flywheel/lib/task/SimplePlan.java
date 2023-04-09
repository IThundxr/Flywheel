package com.jozufozu.flywheel.lib.task;

import java.util.List;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

public record SimplePlan(List<Runnable> parallelTasks) implements Plan {
	@Override
	public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
		if (parallelTasks.isEmpty()) {
			onCompletion.run();
			return;
		}

		var synchronizer = new Synchronizer(parallelTasks.size(), onCompletion);
		for (Runnable task : parallelTasks) {
			taskExecutor.execute(() -> {
				task.run();
				synchronizer.decrementAndEventuallyRun();
			});
		}
	}
}
