package io.spring.batch.helloworld;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.concurrent.Callable;

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
				.listener(promotionListener())
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

			/**
			 * job 전체 공유
			 */
			ExecutionContext jobContext1 =
					chunkContext
							.getStepContext()
							.getStepExecution()
							.getJobExecution()
							.getExecutionContext();

			jobContext1.put("user.name1", name);

			/**
			 * 해당 step만 공유
			 */
			ExecutionContext jobContext2 =
					chunkContext
							.getStepContext()
							.getStepExecution()
							.getExecutionContext();

			jobContext2.put("user.name2", name);

			System.out.println(String.format("Hello, %s!", name));
			return RepeatStatus.FINISHED;
		});
	}

	/**
	 * 늦은 바인딩을 통해 JobParameters 접근하기
	 */
	@StepScope
	@Bean
	public Tasklet helloWorldTasklet3(
			@Value("#{jobParameters['name']}") String name, @Value("#{jobParameters['fileName']}") String fileName) {
		return ((stepContribution, chunkContext) -> {

			System.out.println(String.format("Hello, %s!", name));
			System.out.println(String.format("file name = %s!", fileName));

			return RepeatStatus.FINISHED;
		});
	}

	/**
	 * 기본으로 제공하는 validator 사용하기
	 */
	@Bean
	public JobParametersValidator validator() {
		DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

		validator.setRequiredKeys(new String[] {"fileName"});
		validator.setOptionalKeys(new String[] {"name"});

		return validator;
	}

	/**
	 * 기본으로 제공하는 validator 사용하기 및 validator 합치기
	 */
	@Bean
	public CompositeJobParametersValidator compositeValidator() {
		CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

		/**
		 * 기본제공 validator
		 */
		DefaultJobParametersValidator defaultJobParametersValidator =
				new DefaultJobParametersValidator(new String[]{"fileName"}, new String[]{"name", "currentDate"});
		defaultJobParametersValidator.afterPropertiesSet();

		validator.setValidators(
				Arrays.asList(new ParameterValidator(), defaultJobParametersValidator)
		);

		return validator;
	}

	/**
	 * 스텝 컨텍스트에 있던 값을 잡 컨텍스트의 값으로 승격시키기
	 */
	@Bean
	public StepExecutionListener promotionListener() {
		ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();

		listener.setKeys(new String[] {"user.name2"});

		return listener;
	}

	/**
	 * job 만들기
	 */
//	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("basicJob")
				/**
				 * 등록해둔 step 장착
				 */
				.start(step2())
				/**
				 * validator 장착
				 */
				.validator(compositeValidator())
				/**
				 * incrementer 장착
				 */
				.incrementer(new DailyJobTimeStamper())
				/**
				 * listener 장착
				 */
				.listener(new JobLoggerListener())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

}
