### AGENT WORKLOG
# Agents and Harness Used
Harness: Codex for Visual Studio Code

Model: GPT-6 Astra

I chose to use Codex for concise responses and code generation.

# Discussion on Use

The first thing I did was have the LLM explain the repo and its tech stack to me. I have not worked with Kotlin before and needed to mentally map the repo to my knowledge of other technology stacks to ensure I understood what I was working with.

Then I asked the LLM to guide me through a single existing feature so I could understand a full trace of the tech stack.

After understanding the tech stack, I figured out where the feature would be best suited, and determined edge cases to test for.

While working with an agent I still followed test-driven development with one change. Instead of the usual test → code → test loop, I front-loaded the test cases before asking the agent to implement the feature.

Just like in typical TDD, the tests act as your verification, but having them all defined also acts as context for your agent to verify itself against.

Tests were designed both by me directly making them and directing the agent to make meaningful variations. Edge cases in the tests focused on common failure areas such as handling empty lists, pagination offsets and limits that would create an empty list, parameters that lead to lists smaller than the given limit, and ensuring the lists of articles are sorted.

I allowed the agent to make the HTTP tests in bulk on its own as they were conceptually simpler, and I could ensure coverage with an easy review of those tests.

 With the tests finalized, I instructed the agent to implement the necessary function within the defined scope and in the appropriate location. It was not allowed to modify the tests after they were finalized.

One thing the agents tend to do is to consider redundant tests, such as checking if default values are passed which is really just a test of the programming language and not a necessary test from a developer standpoint. So test writing needs to remain a very human-involved part of development to ensure tests are relevant and useful.

If I were doing this again in a production repository, I would add regression and contract testing around affected boundaries before allowing the agent to make broader changes. This repository contained several unfinished features outside the task scope, so I deliberately kept my changes isolated rather than expanding into unrelated areas.