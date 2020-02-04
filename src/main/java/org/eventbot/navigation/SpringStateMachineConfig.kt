package org.eventbot.navigation

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.annotation.OnTransition
import org.springframework.statemachine.annotation.WithStateMachine
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer
import java.util.*


@Configuration
@EnableStateMachine
class SpringStateMachineConfig : EnumStateMachineConfigurerAdapter<SpringState, SpringEvent>() {
    override fun configure(states: StateMachineStateConfigurer<SpringState, SpringEvent>) {
        states
                .withStates()
                .initial(SpringState.S_0_MAIN)
                .states(EnumSet.allOf(SpringState::class.java))
    }

    override fun configure(transitions: StateMachineTransitionConfigurer<SpringState, SpringEvent>) {
        transitions
                .withExternal()
                .source(SpringState.S_0_MAIN).target(SpringState.S_1_GROUPS)
                .event(SpringEvent.OnGroups)
                .and()
                .withExternal()
                .source(SpringState.S_1_GROUPS).target(SpringState.S_0_MAIN)
                .event(SpringEvent.OnBack)
                .and()
                .withExternal()
                .source(SpringState.S_1_GROUPS).target(SpringState.S_2_GROUPS_ALL)
                .event(SpringEvent.OnAllGroups)
                .and()
                .withExternal()
                .source(SpringState.S_1_GROUPS).target(SpringState.S_2_GROUP_ADMIN)
                .event(SpringEvent.OnMyGroups)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUPS_ALL).target(SpringState.S_1_GROUPS)
                .event(SpringEvent.OnBack)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUPS_ALL).target(SpringState.S_2_GROUPS_ALL)
                .event(SpringEvent.OnGroupLunch)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUPS_ALL).target(SpringState.S_3_GROUP_INFO)
                .event(SpringEvent.OnGroupInfo)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUPS_ALL).target(SpringState.S_2_GROUPS_ALL)
                .event(SpringEvent.OnGroupLeave)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUP_ADMIN).target(SpringState.S_1_GROUPS)
                .event(SpringEvent.OnBack)
                .and()
                .withExternal()
                .source(SpringState.S_2_GROUP_ADMIN).target(SpringState.S_2_GROUP_ADMIN)
                .event(SpringEvent.OnGroupRename)
    }

    @WithStateMachine
    internal class MyBean {
        private val log = LoggerFactory.getLogger(javaClass)
        @OnTransition(target = ["S_1_GROUPS"])
        fun toGroups1() {
            log.info("Transition to S_1_GROUPS done!")
        }
    }
}