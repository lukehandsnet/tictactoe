# Setting Up OpenHands in Your Repository

This repository is configured with OpenHands, an AI assistant that can help with code reviews, answer questions, and assist with development tasks.

## Required Secrets

To use OpenHands, you need to set up the following GitHub repository secrets:

1. `LLM_API_KEY` - Your API key for the LLM service (required)
2. `PAT_TOKEN` - A GitHub Personal Access Token with appropriate permissions (optional, falls back to GITHUB_TOKEN)
3. `PAT_USERNAME` - The username associated with the PAT_TOKEN (optional, defaults to 'openhands-agent')
4. `LLM_BASE_URL` - Custom base URL for the LLM API (optional)

## Setting Up Secrets

1. Go to your repository on GitHub
2. Click on "Settings" > "Secrets and variables" > "Actions"
3. Click on "New repository secret"
4. Add each of the required secrets

## Repository Variables (Optional)

You can customize OpenHands behavior by setting the following repository variables:

1. `OPENHANDS_MACRO` - Custom trigger word (default: '@openhands-agent')
2. `OPENHANDS_MAX_ITER` - Maximum iterations for the resolver (default: 50)
3. `LLM_MODEL` - LLM model to use (default: 'anthropic/claude-3-5-sonnet-20241022')
4. `TARGET_BRANCH` - Target branch for PRs (default: 'main')

## How to Use OpenHands

### Using the Resolver

The resolver can automatically fix issues in your repository. To use it:

1. Label an issue with `fix-me` to have OpenHands attempt to fix it automatically
2. Comment on an issue or PR with `@openhands-agent` to invoke OpenHands

### Using the Assistant

The assistant can help with code reviews and answer questions. To use it:

1. Comment on a PR with `/openhands` to have OpenHands review your code
2. OpenHands will automatically review new pull requests

## Examples

- Label an issue with `fix-me` to have OpenHands fix it
- Comment `@openhands-agent can you fix this bug?` on an issue
- Comment `/openhands what does this code do?` on a PR