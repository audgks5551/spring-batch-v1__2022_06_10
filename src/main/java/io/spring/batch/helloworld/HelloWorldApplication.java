package io.spring.batch.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing // spring batch 활성화
@SpringBootApplication
public class HelloWorldApplication {
	@Autowired
	private JobBuilderFactory jobBuilderFactory; // job 공장
	@Autowired
	private StepBuilderFactory stepBuilderFactory; // step 공장

	/**
	 * hello world를 출력하고 종료하는 step 만들기
	 */
	@Bean
	public Step step() {
		return this.stepBuilderFactory.get("step1")
				.tasklet(new Tasklet() {
					/**
					 * 실행 구간
					 */
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

						System.out.println("Hello, World");

						return RepeatStatus.FINISHED;
					}
				}).build();
	}

	/**
	 * job 만들기
	 */
	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job")
				/**
				 * 등록해둔 step 장착
				 */
				.start(step())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

}
