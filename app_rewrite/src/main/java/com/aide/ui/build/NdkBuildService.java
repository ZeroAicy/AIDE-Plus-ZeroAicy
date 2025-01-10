/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */

//
// Decompiled by Jadx - 1257ms
//
package com.aide.ui.build;

import abcd.hy;
import abcd.wf;
import abcd.xf;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Keep;
import com.aide.common.AppLog;
import com.aide.common.StreamUtilities;
import com.aide.engine.SyntaxError;
import com.aide.ui.AppPreferences;
import com.aide.ui.ServiceContainer;
import com.aide.ui.build.android.NdkConfiguration;
import com.aide.ui.build.android.g;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.services.ProjectService;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.cmake.CmakeBuild;
import io.github.zeroaicy.aide.cmake.ProcessExitInfo;
import io.github.zeroaicy.aide.cmake.ProcessUtil;
import io.github.zeroaicy.aide.extend.ZeroAicyExtensionInterface;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.FileUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import io.github.zeroaicy.aide.utils.ZeroAicyTermuxShellEnvironment;
import android.app.PendingIntent;
import android.content.Intent;

public class NdkBuildService {
	public static final String TAG = "NdkBuildService";
	
	static ZeroAicyTermuxShellEnvironment termuxShellEnvironment = ZeroAicyTermuxShellEnvironment.getInstance();
		
	private RunNdkBuildFutureTask runNdkBuildFutureTask;

	private g FH;

	private final ExecutorService executorService;

	public NdkBuildService() {
		this.executorService = ZeroAicyExtensionInterface.getProjectExecutorService();
	}
	public static PendingIntent j6(Context context, int i, Intent intent, int i2) {
        i2 |= PendingIntent.FLAG_MUTABLE;
		return PendingIntent.getActivity(context, i, intent, i2);
    }
	static void DW(NdkBuildService ndkBuildService, Map<String, List<SyntaxError>> map) {
		ndkBuildService.we(map);
	}

	private void EQ() {
		if (this.FH != null) {
			this.FH.J0();
		}
	}

	static void FH(NdkBuildService ndkBuildService) {
		ndkBuildService.EQ();
	}

	static void Hw(NdkBuildService ndkBuildService, Throwable th) {
		ndkBuildService.tp(th);
	}

	private SyntaxError VH(String str, int i, int i2, String str2) {
		SyntaxError syntaxError = new SyntaxError();
		syntaxError.jw = i;
		syntaxError.fY = i2;
		syntaxError.qp = i;
		syntaxError.k2 = 1000;
		syntaxError.zh = str + ": " + str2;
		return syntaxError;
	}

	static Map<String, List<SyntaxError>> Zo(NdkBuildService ndkBuildService, String str, String str2) {
		return ndkBuildService.gn(str, str2);
	}

	private Map<String, List<SyntaxError>> gn(String str, String str2) {
		HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();

		for (String errorLine : str2.split("\n")) {
			errorLine = errorLine.trim();

			if (errorLine.length() <= 0) {
				continue;
			}
			try {
				int indexOf = errorLine.indexOf(':');

				if (indexOf > 0) {
					String path = new File(str, errorLine.substring(0, indexOf)).getPath();

					if (FileSystem.KD(path)) {
						int i2 = indexOf + 1;
						int indexOf2 = errorLine.indexOf(58, i2);
						if (indexOf2 < 0) {
							indexOf2 = errorLine.indexOf(32, i2);
						}
						if (indexOf2 > 0) {
							int i;

							try {
								i = Integer.parseInt(errorLine.substring(i2, indexOf2));
							} catch (NumberFormatException unused) {
								i = 1;
							}
							int i3 = indexOf2 + 1;
							int indexOf3 = errorLine.indexOf(58, i3);
							if (indexOf3 > 0) {
								try {
									Integer.parseInt(errorLine.substring(i3, indexOf3));
								} catch (NumberFormatException unused2) {
								}
							}
							String trim2 = errorLine.substring(indexOf3 + 1, errorLine.length()).trim();
							if (trim2.startsWith("error:")) {
								SyntaxError VH = VH("NDK", i, 1, trim2.substring(6, trim2.length()).trim());
								if (!hashMap.containsKey(path)) {
									hashMap.put(path, new ArrayList<SyntaxError>());
								}
								hashMap.get(path).add(VH);
							}
						}
					}
				}
			} catch (Exception e) {
				AppLog.e(e);
			}

			if (!hashMap.containsKey(str)) {
				hashMap.put(str, new ArrayList<>());
			}
			hashMap.get(str).add(VH("NDK", 1, 1, errorLine));

		}
		return hashMap;

	}

	static void j6(NdkBuildService ndkBuildService, boolean z) {
		ndkBuildService.u7(z);
	}

	private void tp(Throwable th) {
		AppLog.e(th);
		if (this.FH != null) {
			this.FH.g3();
		}
	}

	private void u7(boolean z) {
		if (this.FH != null) {
			this.FH.vJ(z);
		}

	}

	static SyntaxError makeSyntaxError(NdkBuildService ndkBuildService, String str, int i, int i2, String str2) {
		return ndkBuildService.VH(str, i, i2, str2);
	}

	private void we(Map<String, List<SyntaxError>> map) {
		if (this.FH != null) {
			this.FH.Mz(map);
		}
	}

	@Keep
	public void J0(boolean z) {
		if (this.runNdkBuildFutureTask != null) {
			this.runNdkBuildFutureTask.cancel(true);
			this.runNdkBuildFutureTask = null;
		}
		RunNdkBuildFutureTask runNdkBuildFutureTask = new RunNdkBuildFutureTask(this, new RunNdkBuildCallable(this, z,
				AppPreferences.isNativeBuildParallel(), ServiceContainer.getProjectService().P8()));

		this.runNdkBuildFutureTask = runNdkBuildFutureTask;
		this.executorService.execute(runNdkBuildFutureTask);

	}

	@Keep
	public void J8(g gVar) {
		this.FH = gVar;
	}

	static class RunNdkBuildFutureTask extends FutureTask<Map<String, List<SyntaxError>>> {
		
		private RunNdkBuildCallable runNdkBuildCallableWB;

		@hy
		final NdkBuildService ndkBuildService;

		public RunNdkBuildFutureTask(NdkBuildService ndkBuildService, RunNdkBuildCallable runNdkBuildCallable) {
			super(runNdkBuildCallable);
			this.ndkBuildService = ndkBuildService;
			this.runNdkBuildCallableWB = runNdkBuildCallable;
		}

		@Override
		protected void done() {
			if (isCancelled()) {
				return;
			}
			boolean z = false;
			try {
				Iterator<String> it = this.runNdkBuildCallableWB.modules.iterator();
				while (it.hasNext()) {
					if (ServiceContainer.getProjectService().g3((String) it.next())) {
						z = true;
					}
				}
				Map<String, List<SyntaxError>> errors = get();
				if (errors == null) {
					NdkBuildService.j6(this.ndkBuildService, z);
				} else {
					NdkBuildService.DW(this.ndkBuildService, errors);
				}
			} catch (InterruptedException unused) {
				NdkBuildService.FH(this.ndkBuildService);
			} catch (ExecutionException e) {
				NdkBuildService.Hw(this.ndkBuildService, e.getCause());
			}
		}
	}

	static class RunNdkBuildCallable implements Callable<Map<String, List<SyntaxError>>> {

		private final boolean isNativeBuildParallel;

		public final List<String> modules;

		@hy
		final NdkBuildService ndkBuildService;

		private final boolean isClean;

		public RunNdkBuildCallable(NdkBuildService ndkBuildService, boolean isClean, boolean isNativeBuildParallel,
				List<String> modules) {
			this.ndkBuildService = ndkBuildService;
			this.isClean = isClean;

			this.isNativeBuildParallel = isNativeBuildParallel;
			this.modules = modules;
		}

		private String DW(byte[] data, int i) {
			String str = "";
			try {
				str = StreamUtilities.readTextReader(new InputStreamReader(new ByteArrayInputStream(data)));
			} catch (Exception unused) {
			}

			String trim = str.trim();
			if (trim.length() != 0) {
				return trim;
			}
			return "ndk-build exited with code " + i;

		}

		// has Android Mk Module
		private boolean hasAndroidMkModule() {

			ProjectService projectService = ServiceContainer.getProjectService();
			for (String module : this.modules) {
				// is Android Mk Module
				if (projectService.g3(module)) {
					return true;
				}
			}
			return false;
		}

		private static void Hw(List<String> list, String str) {
			
			
			StringBuilder sb = new StringBuilder();

			sb.append("Running ndk-build [" + str + "] ");

			for (int i = 0; i < list.size(); i++) {
				sb.append('\"');
				sb.append(list.get(i));
				sb.append('\"');
				if (i != list.size() - 1) {
					sb.append(" ");
				}
			}
			AppLog.d(TAG, sb.toString());
		}

		private Map<String, List<SyntaxError>> runNdkBuild(String str, boolean isNativeBuildParallel) {

			ProjectService projectService = ServiceContainer.getProjectService();

			for (String module : this.modules) {
				// isAndroidMkModule
				if (projectService.g3(module)) {
					
					int threadCount = isNativeBuildParallel ? 4 : 1;
					
					// 移除 TARGET_AR=$(TOOLCHAIN_PREFIX)ar
					List<String> ndkConfiguration = NdkConfiguration.VH(str, threadCount);
					ndkConfiguration.remove(ndkConfiguration.size() - 2);
					
					List<String> ndkBuildArgs = termuxShellEnvironment.setupShellCommandArguments(ndkConfiguration);
					
					//  只有PATH
					Map<String, String> env = NdkConfiguration.gn();
					
					Map<String, String> termuxEnvironment = termuxShellEnvironment.getEnvironment(false, env);
					env = termuxEnvironment.isEmpty() ? env : termuxEnvironment;

					Hw(ndkBuildArgs, module);
					
					// 运行ndk-build
					wf j6 = xf.j6(ndkBuildArgs, module, env, true, (OutputStream) null, (byte[]) null);
					
					if (j6.DW() != 0) {
						return NdkBuildService.Zo(this.ndkBuildService, module, DW(j6.j6(), j6.DW()));
					}
				}

			}

			return null;
		}

		@Override
		public Map<String, List<SyntaxError>> call() {

			{
				Map<String, List<SyntaxError>> compileSyntaxErrors = runCmakeBuild();
				if (compileSyntaxErrors != null) {
					return compileSyntaxErrors;
				}

			}
			// 所有module没有 Android.mk项目
			if (!hasAndroidMkModule()) {
				return null;
			}

			// 没有安装Ndk
			if (!NdkConfiguration.isInstalledNdk()) {
				HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();
				String module = this.modules.get(0);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ServiceContainer.isX86()) {

					hashMap.put(module, new ArrayList<SyntaxError>());

					SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
							"Native development is not supported on X86 devices running Android 10 and above.");
					SyntaxError syntaxError = makeSyntaxError;

					hashMap.get(module).add(syntaxError);
				} else {

					hashMap.put(module, new ArrayList<SyntaxError>());

					SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
							"NDK support not installed.");
					hashMap.get(module).add(makeSyntaxError);
				}
				return hashMap;
			}

			// busybox适配 从com.aide.ndk29创建软连接到 PATH
			// NdkConfiguration.U2();

			if (this.isClean) {
				// ndk-build clean
				Map<String, List<SyntaxError>> syntaxErrors = runNdkBuild("clean", false);
				if (syntaxErrors != null) {
					return syntaxErrors;
				}
			}

			long currentTimeMillis = System.currentTimeMillis();

			Map<String, List<SyntaxError>> compileSyntaxErrors = runNdkBuild(null, this.isNativeBuildParallel);

			AppLog.d("NDK build elapsed " + (System.currentTimeMillis() - currentTimeMillis) + "ms");

			return compileSyntaxErrors;

		}

		/*
		 * 遍历module 运行cmake
		 */

		private static final ZeroAicyBuildGradle singleton = ZeroAicyBuildGradle.getSingleton();

		private Map<String, List<SyntaxError>> runCmakeBuild() {

			Context context = ContextUtil.getContext();
			String androidSdkBaseDir = "framework/android-sdk";

			boolean isPro = context.getPackageName().equals("aidepro.top");
			if (isPro) {
				androidSdkBaseDir = "framework/android-sdk";
			} else {
				androidSdkBaseDir = "home/android-sdk";
			}

			String androidSdkPath = new File(context.getFilesDir(), androidSdkBaseDir).getAbsolutePath();

			ProjectService projectService = ServiceContainer.getProjectService();

			for (String modulePath : this.modules) {

				// 是否是 cmake项目

				// gradle项目
				if (GradleTools.isGradleProject(modulePath)) {
					// 
					String buildGradlePath = GradleTools.getBuildGradlePath(modulePath);
					if (!FileSystem.exists(buildGradlePath)) {
						// 存在build.gradle
						// 错误的 gradle 项目
						continue;
					}

					ZeroAicyBuildGradle configuration = singleton.getConfiguration(buildGradlePath);

					String projectPath = modulePath;

					// 默认 "src/main/cpp/CMakeLists.txt"
					String cmakeListsTxtPath = configuration.getCmakeListsTxtPath("src/main/cpp/CMakeLists.txt");

					if (!new File(projectPath, cmakeListsTxtPath).exists()) {
						// 文件不存在，不是cmake项目
						continue;
					}

					// 计算 CMakeLists.txt 父目录
					if (cmakeListsTxtPath.endsWith("CMakeLists.txt")) {
						cmakeListsTxtPath = FileSystem.getParent(cmakeListsTxtPath);
					}

					// cmake缓存路径相对于 projectPath
					String cmakeBuildCachePath = "build/bin/intermediates/cmake";

					// android 版本
					String minSdkVersion = configuration.getMinSdkVersion(null);
					// AppLog.println_d("minSdkVersion %s", minSdkVersion);
					
					// cmake版本
					String cmakeVersion = configuration.getCmakeVersion();
					// ndk版本
					String ndkVersion = configuration.getNdkVersion();
					
					// 待编译 abi
					LinkedHashSet<String> cmakeAbiFilters = configuration.getCmakeAbiFilters();

					CmakeBuild.Builder builder = new CmakeBuild.Builder()
							// ndk版本
							.setNdkVersion(ndkVersion)
							// 指定cmake版本
							.setCmakeVersion(cmakeVersion)
							
							// 项目路径
							.setProjectPath(projectPath)
							// 指定安卓版本
							.setSystemVersion(minSdkVersion)
							// 输出目录
							.setCmakeOutputDirectoryPath("src/main/jniLibs")
							// 必须在setCmakeOutputDirectoryPath之后调用
							// 否则被覆盖
							.setCmakeBuildCachePath(cmakeBuildCachePath)
							// 源码路径
							.setCmakeListsTxtPath(cmakeListsTxtPath)

							// 指定ndk所在父目录 android-sdk
							// 空值会自动指定 CMAKE_VERSION 与 NDK_VERSION
							.setAndroidSdkPath(androidSdkPath);

					if (this.isClean) {
						// 清除
						FileUtil.deleteFolder(new File(projectPath, builder.getCmakeBuildCachePath()));
					}

					for (String abi : cmakeAbiFilters) {

						// 指定构建ABI
						builder.setAndroidABI(abi)
								// 指定安卓版本
								.setSystemVersion(minSdkVersion);

						CmakeBuild build = builder.build();
						ProcessExitInfo runCmakeBuildInfo = runCmakeBuild(build, projectPath);
						if (runCmakeBuildInfo != null) {
							if (runCmakeBuildInfo.exit() != 0) {
								// make
								return NdkBuildService.Zo(
										// 
										this.ndkBuildService, projectPath,
										DW(runCmakeBuildInfo.getMessagen(), runCmakeBuildInfo.exit()));
							}
						}
					}
					continue;
				}

				// 非gradle项目 cpp/CMakeLists.txt | "src/main/cpp/CMakeLists.txt"

			}

			return null;
		}

		private static ProcessExitInfo runCmakeBuild(final CmakeBuild build, String projectPath) {

			if (build.error()) {
				return new ProcessExitInfo() {
					@Override
					public int exit() {
						return -1;
					}
					@Override
					public byte[] getMessagen() {
						return build.getBuildInfo().getBytes();
					}
				};
			}
			
			Map<String, String> termuxEnvironment = termuxShellEnvironment.getEnvironment(false);

			//进程信息
			Map<String, String> env = termuxEnvironment.isEmpty() ? System.getenv() : termuxEnvironment;
			
			List<String> commandList = termuxShellEnvironment.setupShellCommandArguments(build.getCmakeCommandList());
			
			ProcessExitInfo processInfo = ProcessUtil.j6(commandList, projectPath, env, true, null, null);

			//			if (!true) {
			//				System.out.println("\n*****debug 编译信息*****");
			//				System.out.println(new String(processInfo.getMessagen()));
			//				if( processInfo.exit() != 0) System.out.println("**********\n\n");
			//			}

			if (processInfo.exit() != 0) {
				return processInfo;
			}
			
			//ninja build.ninja
			List<String> ninjaCommandList = termuxShellEnvironment.setupShellCommandArguments(build.getNinjaCommandList());
			processInfo = ProcessUtil.j6(ninjaCommandList, projectPath, env, true, null, null);

			//			if (!true) {
			//				System.out.println("\n*****debug ninja运行信息 *****");
			//				System.out.println(new String(processInfo.getMessagen()));
			//				System.out.println("**********\n\n");
			//			}

			if (processInfo.exit() != 0) {
				return processInfo;
			}

			return null;
		}

	}
}

