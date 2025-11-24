select
    `t`.`id` AS `id`,`t`.`name` AS `name`,
    `t`.`description` AS `description`,
    `t`.`need_skills` AS `need_skills`,
    `c`.`title` AS `title`,
    `c`.`category` AS `category`,
    `u`.`username` AS `username`,
    `u`.`real_name` AS `real_name`,
       coalesce(`u`.`real_name`,`u`.`username`) AS `leader_display_name`,
       (select count(0) from `team_members` `tm` where (`tm`.`team_id` = `t`.`id`)) AS `team_member_count`,
       group_concat(distinct `us`.`skill` order by `us`.`skill` ASC separator ', ') AS `team_skills`,group_concat(distinct `tm`.
           `user_id` order by `tm`.`user_id` ASC separator ',') AS `member_ids` from ((((`teams` `t` join `competitions` `c`
               on((`t`.`competition_id` = `c`.`id`))) join `users` `u` on((`t`.`leader_id` = `u`.`id`))) left join `team_members` `tm`
               on((`t`.`id` = `tm`.`team_id`))) left join `user_skills` `us` on((`tm`.`user_id` = `us`.`user_id`))) group by `t`.`id`,
                `t`.`name`,`t`.`description`,`t`.`need_skills`,`c`.`title`,`c`.`category`,`u`.`username`,`u`.`real_name` order by `t`.`id` desc



select `tm`.`team_id` AS `id`,`t`.`name` AS `team_name`,`t`.`description` AS `description`,`t`.`need_skills`
       `tm`.`user_id` AS `user_id`,`tm`.`role` AS `role`,`tm`.`joined_at` AS
           `joined_at`,`tm`.`status` AS `status`,group_concat(distinct `us`.`skill`
               order by `us`.`skill` ASC separator ',') AS `skills`,
    group_concat(distinct concat(`uh`.`honour_title`,'(',
        date_format(`uh`.`obtained_time`,'%Y-%m-%d'),')','[',ifnull(`uh`.`certificate_image_url`,''),']') order by
        `uh`.`obtained_time` ASC separator ',') AS `honours` from (((`team_members` `tm` join `teams` `t`
            on((`tm`.`team_id` = `t`.`id`))) left join `user_skills` `us` on((`tm`.`user_id` = `us`.`user_id`))) left join
            `user_honours` `uh` on((`tm`.`user_id` = `uh`.`user_id`))) group by `tm`.`team_id`,`t`.`name`,`tm`.`user_id`,
                                                                                `tm`.`role`,`tm`.`joined_at`,`tm`.`status`


select
    `tm`.`team_id` AS `id`,
    `t`.`name` AS `name`,
    `t`.`description` AS `description`,
    `t`.`need_skills` AS `need_skills`,
    `tm`.`user_id` AS `user_id`,
    `tm`.`role` AS `role`,
    `tm`.`joined_at` AS `joined_at`,
    `tm`.`status` AS `status`,
    group_concat(distinct `us`.`skill` order by `us`.`skill` ASC separator ', ') AS `team_skills`,
    group_concat(
        distinct concat(
            `uh`.`honour_title`, '(', date_format(`uh`.`obtained_time`, '%Y-%m-%d'), ')',
            '[', ifnull(`uh`.`certificate_image_url`, ''), ']'
        ) order by `uh`.`obtained_time` ASC separator ','
    ) AS `honours`
from
    `team_members` `tm`
        join `teams` `t` on (`tm`.`team_id` = `t`.`id`)
        left join `user_skills` `us` on (`tm`.`user_id` = `us`.`user_id`)
        left join `user_honours` `uh` on (`tm`.`user_id` = `uh`.`user_id`)
group by
    `tm`.`team_id`,
    `t`.`name`,
    `t`.`description`,
    `t`.`need_skills`,
    `tm`.`user_id`,
    `tm`.`role`,
    `tm`.`joined_at`,
    `tm`.`status`;
