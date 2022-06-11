package io.spring.batch.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.tasklet(((stepContribution, chunkContext) -> {
					System.out.println("Hello, World!");
					return RepeatStatus.FINISHED;
				})).build();
	}

	@Bean
	public Step step2() {
		return this.stepBuilderFactory.get("step2")
				.tasklet(helloWorldTasklet2(null))
				.build();
	}

	/**
	 * spring 구성을 사용해 JobParameters에 접근하기
	 */
	@Bean
	public Tasklet helloWorldTasklet1() {
		return ((stepContribution, chunkContext) -> {
			String name = (String) chunkContext.getStepContext()
					.getJobParameters()
					.get("name");

			System.out.println(String.format("Hello, %s", name));
			return RepeatStatus.FINISHED;
		});
	}

	/**
	 * 늦은 바인딩을 통해 JobParameters 접근하기
	 */
	@StepScope
	@Bean
	public Tasklet helloWorldTasklet2(@Value("#{jobParameters['name']}") String name) {
		return ((stepContribution, chunkContext) -> {
			System.out.println(String.format("Hello, %s!", name));
			return RepeatStatus.FINISHED;
		});
	}


	/**
	 * job 만들기
	 */
	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("basicJob")
				/**
				 * 등록해둔 step 장착
				 */
				.start(step2())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

}
