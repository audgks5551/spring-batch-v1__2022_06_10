package io.spring.batch.helloworld;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.SystemProcessExitCodeMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

//@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SystemCommandJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("systemCommandJob")
                .start(systemCommandStep())
                .build();
    }

    @Bean
    public Step systemCommandStep() {
        return this.stepBuilderFactory.get("systemCommandStep")
                .tasklet(systemCommandTasklet())
                .build();
    }

    @Bean
    public SystemCommandTasklet systemCommandTasklet() {
        SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();

        systemCommandTasklet.setCommand("touch tmp.txt"); // 명령어 수행
        systemCommandTasklet.setTimeout(5000); // 타임아웃 설정
        systemCommandTasklet.setInterruptOnCancel(true); // 취소시 인터셉터 설정

        systemCommandTasklet.setWorkingDirectory("/tmp"); // `cd /tmp`와 동일

        systemCommandTasklet.setSystemProcessExitCodeMapper(touchCodeMapper()); // 시스템 반환코드를 스프링 배치 상태 값으로 매핑
        systemCommandTasklet.setTerminationCheckInterval(5000); // 완료여부를 주기적으로 확인
        systemCommandTasklet.setTaskExecutor(new SimpleAsyncTaskExecutor()); // 비동기 설정 (동기식은 락 걸릴 수 있음)
        systemCommandTasklet.setEnvironmentParams(new String[] {
                "JAVA_HOME=/java",
                "BATCH_HOME=/Users/batch"
        }); // 환경 파라미터 목록

        return systemCommandTasklet;
    }

    @Bean
    public SystemProcessExitCodeMapper touchCodeMapper() {
        return new SimpleSystemProcessExitCodeMapper(); // 반환된 시스템 코드가 0이면 ExitStatus.FINISHED, 아니면 ExitStatus.FAILED 반환
    }
}
